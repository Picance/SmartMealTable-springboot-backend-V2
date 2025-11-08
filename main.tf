# Provider 설정
provider "aws" {
  region = "ap-northeast-2"
}

# VPC 생성
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "smartmealtable-vpc"
  }
}

# 인터넷 게이트웨이
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "smartmealtable-igw"
  }
}

# Public 서브넷
resource "aws_subnet" "public_1" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.3.0/24"
  availability_zone       = "ap-northeast-2a"
  map_public_ip_on_launch = true

  tags = {
    Name = "smartmealtable-public-1"
  }
}

# 두 번째 Public 서브넷
resource "aws_subnet" "public_2" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.4.0/24"
  availability_zone       = "ap-northeast-2c"
  map_public_ip_on_launch = true

  tags = {
    Name = "smartmealtable-public-2"
  }
}

# Private 서브넷 (RDS 전용)
resource "aws_subnet" "private_1" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "ap-northeast-2a"
  map_public_ip_on_launch = false

  tags = { Name = "smartmealtable-private-1" }
}

resource "aws_subnet" "private_2" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = "ap-northeast-2c"
  map_public_ip_on_launch = false

  tags = { Name = "smartmealtable-private-2" }
}

# Public 라우팅 테이블
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "smartmealtable-public-rt"
  }
}

# Public 서브넷 라우팅 연결
resource "aws_route_table_association" "public_1" {
  subnet_id      = aws_subnet.public_1.id
  route_table_id = aws_route_table.public.id
}

# Public 서브넷 라우팅 연결 (두 번째 서브넷용)
resource "aws_route_table_association" "public_2" {
  subnet_id      = aws_subnet.public_2.id
  route_table_id = aws_route_table.public.id
}

# 보안 그룹 - EC2
resource "aws_security_group" "ec2" {
  name        = "smartmealtable-ec2-sg"
  description = "Security group for Smartmealtable EC2"
  vpc_id      = aws_vpc.main.id

  # Spring Boot 애플리케이션용 8080 포트
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow Spring Boot application"
  }

  # Admin 애플리케이션용 8081 포트
  ingress {
    from_port   = 8081
    to_port     = 8081
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow Admin application"
  }

  # Batch Crawler 애플리케이션용 8082 포트 (VPC 내부 통신만)
  ingress {
    from_port   = 8082
    to_port     = 8082
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]
    description = "Allow Batch Crawler within VPC"
  }

  # SSH 접속
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Prometheus
  ingress {
    from_port   = 9090
    to_port     = 9090
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Grafana
  ingress {
    from_port   = 3000
    to_port     = 3000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Redis (선택적)
  ingress {
    from_port   = 6379
    to_port     = 6379
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]
    description = "Allow Redis within VPC"
  }

  # 아웃바운드 트래픽 허용
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "smartmealtable-ec2-sg"
  }
}

# IAM 역할 생성
resource "aws_iam_role" "ec2_role" {
  name = "smartmealtable-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

# SSM 관리형 정책 연결
resource "aws_iam_role_policy_attachment" "ssm_policy" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# ECR 정책 추가
resource "aws_iam_role_policy" "ecr_policy" {
  name = "ecr-policy"
  role = aws_iam_role.ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage"
        ]
        Resource = "*"
      }
    ]
  })
}

# EC2용 인스턴스 프로파일 생성
resource "aws_iam_instance_profile" "ec2_profile" {
  name = "smartmealtable-ec2-profile"
  role = aws_iam_role.ec2_role.name
}

# EC2 인스턴스 1 - API 서버
resource "aws_instance" "api" {
  ami           = "ami-0e9bfdb247cc8de84"  # Ubuntu 22.04 LTS AMI
  instance_type = "t3.micro"
  subnet_id     = aws_subnet.public_1.id
  
  # 명시적 공개 IP 할당
  associate_public_ip_address = true

  # 세부 모니터링 활성화
  monitoring = true

  vpc_security_group_ids = [aws_security_group.ec2.id]
  key_name              = aws_key_pair.smartmealtable.key_name
  iam_instance_profile  = aws_iam_instance_profile.ec2_profile.name

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  user_data = <<-EOF
              #!/bin/bash
              set -e

              # SSM Agent 설치
              sudo snap install amazon-ssm-agent --classic
              sudo systemctl start snap.amazon-ssm-agent.amazon-ssm-agent.service
              sudo systemctl enable snap.amazon-ssm-agent.amazon-ssm-agent.service

              # AWS CLI 설치
              sudo apt-get update
              sudo apt-get install -y unzip
              curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
              unzip awscliv2.zip
              sudo ./aws/install

              # Docker 설치
              sudo apt-get install -y ca-certificates curl gnupg
              sudo install -m 0755 -d /etc/apt/keyrings
              curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
              sudo chmod a+r /etc/apt/keyrings/docker.gpg
              echo "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
              sudo apt-get update
              sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
              sudo usermod -aG docker ubuntu
              sudo systemctl start docker
              sudo systemctl enable docker

              # Docker Compose 설치
              sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
              sudo chmod +x /usr/local/bin/docker-compose

              # CloudWatch Agent 설치 및 설정
              sudo wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
              sudo dpkg -i amazon-cloudwatch-agent.deb

              # CloudWatch Agent 설정
              sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/etc/
              sudo bash -c 'cat > /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json' << 'EOT'
              {
                "agent": {
                  "metrics_collection_interval": 60,
                  "run_as_user": "root"
                },
                "metrics": {
                  "append_dimensions": {
                    "InstanceId": "$${aws:InstanceId}",
                    "InstanceType": "$${aws:InstanceType}",
                    "AutoScalingGroupName": "$${aws:AutoScalingGroupName}"
                  },
                  "metrics_collected": {
                    "mem": {
                      "measurement": [
                        "mem_used_percent"
                      ],
                      "metrics_collection_interval": 60
                    },
                    "swap": {
                      "measurement": [
                        "swap_used_percent"
                      ]
                    }
                  }
                }
              }
              EOT

              # CloudWatch Agent 시작
              sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -s -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json
              sudo systemctl start amazon-cloudwatch-agent
              sudo systemctl enable amazon-cloudwatch-agent
              EOF

  tags = {
    Name = "smartmealtable-api"
    Role = "api"
  }
}

# EC2 인스턴스 2 - Admin + Redis + 모니터링
resource "aws_instance" "admin" {
  ami           = "ami-0e9bfdb247cc8de84"  # Ubuntu 22.04 LTS AMI
  instance_type = "t3.micro"
  subnet_id     = aws_subnet.public_2.id
  
  # 명시적 공개 IP 할당
  associate_public_ip_address = true

  # 세부 모니터링 활성화
  monitoring = true

  vpc_security_group_ids = [aws_security_group.ec2.id]
  key_name              = aws_key_pair.smartmealtable.key_name
  iam_instance_profile  = aws_iam_instance_profile.ec2_profile.name

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  user_data = <<-EOF
              #!/bin/bash
              set -e

              # SSM Agent 설치
              sudo snap install amazon-ssm-agent --classic
              sudo systemctl start snap.amazon-ssm-agent.amazon-ssm-agent.service
              sudo systemctl enable snap.amazon-ssm-agent.amazon-ssm-agent.service

              # AWS CLI 설치
              sudo apt-get update
              sudo apt-get install -y unzip
              curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
              unzip awscliv2.zip
              sudo ./aws/install

              # Docker 설치
              sudo apt-get install -y ca-certificates curl gnupg
              sudo install -m 0755 -d /etc/apt/keyrings
              curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
              sudo chmod a+r /etc/apt/keyrings/docker.gpg
              echo "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
              sudo apt-get update
              sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
              sudo usermod -aG docker ubuntu
              sudo systemctl start docker
              sudo systemctl enable docker

              # Docker Compose 설치
              sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
              sudo chmod +x /usr/local/bin/docker-compose

              # CloudWatch Agent 설치 및 설정
              sudo wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
              sudo dpkg -i amazon-cloudwatch-agent.deb

              # CloudWatch Agent 설정
              sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/etc/
              sudo bash -c 'cat > /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json' << 'EOT'
              {
                "agent": {
                  "metrics_collection_interval": 60,
                  "run_as_user": "root"
                },
                "metrics": {
                  "append_dimensions": {
                    "InstanceId": "$${aws:InstanceId}",
                    "InstanceType": "$${aws:InstanceType}",
                    "AutoScalingGroupName": "$${aws:AutoScalingGroupName}"
                  },
                  "metrics_collected": {
                    "mem": {
                      "measurement": [
                        "mem_used_percent"
                      ],
                      "metrics_collection_interval": 60
                    },
                    "swap": {
                      "measurement": [
                        "swap_used_percent"
                      ]
                    }
                  }
                }
              }
              EOT

              # CloudWatch Agent 시작
              sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -s -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json
              sudo systemctl start amazon-cloudwatch-agent
              sudo systemctl enable amazon-cloudwatch-agent
              EOF

  tags = {
    Name = "smartmealtable-admin"
    Role = "admin"
  }
}

# EC2 인스턴스 3 - Scheduler + Crawler (배치 작업)
resource "aws_instance" "batch" {
  ami           = "ami-0e9bfdb247cc8de84"  # Ubuntu 22.04 LTS AMI
  instance_type = "t3.micro"
  subnet_id     = aws_subnet.public_1.id
  
  # 명시적 공개 IP 할당
  associate_public_ip_address = true

  # 세부 모니터링 활성화
  monitoring = true

  vpc_security_group_ids = [aws_security_group.ec2.id]
  key_name              = aws_key_pair.smartmealtable.key_name
  iam_instance_profile  = aws_iam_instance_profile.ec2_profile.name

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  user_data = <<-EOF
              #!/bin/bash
              set -e

              # SSM Agent 설치
              sudo snap install amazon-ssm-agent --classic
              sudo systemctl start snap.amazon-ssm-agent.amazon-ssm-agent.service
              sudo systemctl enable snap.amazon-ssm-agent.amazon-ssm-agent.service

              # AWS CLI 설치
              sudo apt-get update
              sudo apt-get install -y unzip
              curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
              unzip awscliv2.zip
              sudo ./aws/install

              # Docker 설치
              sudo apt-get install -y ca-certificates curl gnupg
              sudo install -m 0755 -d /etc/apt/keyrings
              curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
              sudo chmod a+r /etc/apt/keyrings/docker.gpg
              echo "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
              sudo apt-get update
              sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
              sudo usermod -aG docker ubuntu
              sudo systemctl start docker
              sudo systemctl enable docker

              # Docker Compose 설치
              sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
              sudo chmod +x /usr/local/bin/docker-compose

              # CloudWatch Agent 설치 및 설정
              sudo wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
              sudo dpkg -i amazon-cloudwatch-agent.deb

              # CloudWatch Agent 설정
              sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/etc/
              sudo bash -c 'cat > /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json' << 'EOT'
              {
                "agent": {
                  "metrics_collection_interval": 60,
                  "run_as_user": "root"
                },
                "metrics": {
                  "append_dimensions": {
                    "InstanceId": "$${aws:InstanceId}",
                    "InstanceType": "$${aws:InstanceType}",
                    "AutoScalingGroupName": "$${aws:AutoScalingGroupName}"
                  },
                  "metrics_collected": {
                    "mem": {
                      "measurement": [
                        "mem_used_percent"
                      ],
                      "metrics_collection_interval": 60
                    },
                    "swap": {
                      "measurement": [
                        "swap_used_percent"
                      ]
                    }
                  }
                }
              }
              EOT

              # CloudWatch Agent 시작
              sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -s -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json
              sudo systemctl start amazon-cloudwatch-agent
              sudo systemctl enable amazon-cloudwatch-agent
              EOF

  tags = {
    Name = "smartmealtable-batch"
    Role = "batch"
  }
}

# 더 이상 사용되지 않음 - 아래 api, admin, batch EIP로 대체됨

# ECR 저장소 생성 - API
resource "aws_ecr_repository" "api" {
  name = "smartmealtable-api"
  force_delete = true

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "smartmealtable-api"
  }
}

# ECR 저장소 생성 - Admin
resource "aws_ecr_repository" "admin" {
  name = "smartmealtable-admin"
  force_delete = true

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "smartmealtable-admin"
  }
}

# ECR 저장소 생성 - Scheduler
resource "aws_ecr_repository" "scheduler" {
  name = "smartmealtable-scheduler"
  force_delete = true

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "smartmealtable-scheduler"
  }
}

# ECR 저장소 생성 - Crawler
resource "aws_ecr_repository" "crawler" {
  name = "smartmealtable-crawler"
  force_delete = true

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "smartmealtable-crawler"
  }
}

# ECR 저장소 정책 - API
resource "aws_ecr_repository_policy" "api_policy" {
  repository = aws_ecr_repository.api.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowPushPull"
        Effect = "Allow"
        Principal = {
          AWS = "*"
        }
        Action = [
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:BatchCheckLayerAvailability",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload"
        ]
      }
    ]
  })
}

# ECR 저장소 정책 - Admin
resource "aws_ecr_repository_policy" "admin_policy" {
  repository = aws_ecr_repository.admin.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowPushPull"
        Effect = "Allow"
        Principal = {
          AWS = "*"
        }
        Action = [
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:BatchCheckLayerAvailability",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload"
        ]
      }
    ]
  })
}

# ECR 저장소 정책 - Scheduler
resource "aws_ecr_repository_policy" "scheduler_policy" {
  repository = aws_ecr_repository.scheduler.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowPushPull"
        Effect = "Allow"
        Principal = {
          AWS = "*"
        }
        Action = [
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:BatchCheckLayerAvailability",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload"
        ]
      }
    ]
  })
}

# ECR 저장소 정책 - Crawler
resource "aws_ecr_repository_policy" "crawler_policy" {
  repository = aws_ecr_repository.crawler.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowPushPull"
        Effect = "Allow"
        Principal = {
          AWS = "*"
        }
        Action = [
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:BatchCheckLayerAvailability",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload"
        ]
      }
    ]
  })
}

# 출력값 수정
output "ecr_api_repository_url" {
  value = aws_ecr_repository.api.repository_url
}

output "ecr_admin_repository_url" {
  value = aws_ecr_repository.admin.repository_url
}

output "ecr_scheduler_repository_url" {
  value = aws_ecr_repository.scheduler.repository_url
}

output "ecr_crawler_repository_url" {
  value = aws_ecr_repository.crawler.repository_url
}

# 키 페어 생성 (사전에 smartmealtable-key.pub이 준비되어 있어야 함)
# SSH 키 생성 방법:
# ssh-keygen -t rsa -b 2048 -f smartmealtable-key -N ""
#
# 주의: Terraform validate/plan을 실행하기 전에 반드시 아래 파일이 있어야 함:
# - smartmealtable-key (private key)
# - smartmealtable-key.pub (public key)

resource "aws_key_pair" "smartmealtable" {
  key_name   = "smartmealtable-key"
  public_key = file("${path.module}/smartmealtable-key.pub")

  tags = {
    Name = "smartmealtable-key-pair"
  }
}

# RDS 보안 그룹
resource "aws_security_group" "rds" {
  name        = "smartmealtable-rds-sg"
  description = "Security group for Smartmealtable RDS"
  vpc_id      = aws_vpc.main.id

  tags = {
    Name = "smartmealtable-rds-sg"
  }
}

# RDS에 대해 EC2에서만 접근 허용
resource "aws_security_group_rule" "rds_from_ec2" {
  type                     = "ingress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  security_group_id        = aws_security_group.rds.id
  source_security_group_id = aws_security_group.ec2.id
}

# RDS 서브넷 그룹
resource "aws_db_subnet_group" "rds" {
  name       = "smartmealtable-rds-subnet-group"
  subnet_ids = [aws_subnet.private_1.id, aws_subnet.private_2.id]

  tags = {
    Name = "smartmealtable-rds-subnet-group"
  }
}

# RDS 향상된 모니터링을 위한 IAM 역할
resource "aws_iam_role" "rds_enhanced_monitoring" {
  name = "smartmealtable-rds-enhanced-monitoring"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "monitoring.rds.amazonaws.com"
        }
      }
    ]
  })
}

# 향상된 모니터링 정책 연결
resource "aws_iam_role_policy_attachment" "rds_enhanced_monitoring" {
  role       = aws_iam_role.rds_enhanced_monitoring.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonRDSEnhancedMonitoringRole"
}

# RDS 인스턴스
resource "aws_db_instance" "smartmealtable" {
  identifier           = "smartmealtable-db"
  engine              = "mysql"
  engine_version      = "8.0"
  instance_class      = "db.t3.micro"
  allocated_storage   = 20
  storage_type        = "gp2"
  
  db_name             = "smartmealtable"
  username           = "smartmeal_user"
  password           = var.db_password
  
  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name   = aws_db_subnet_group.rds.name
  
  skip_final_snapshot    = true
  publicly_accessible    = false
  
  monitoring_interval = 60
  monitoring_role_arn = aws_iam_role.rds_enhanced_monitoring.arn
  
  tags = {
    Name = "smartmealtable-db"
  }
}

# RDS 엔드포인트 출력
output "rds_endpoint" {
  value       = "${aws_db_instance.smartmealtable.address}:${aws_db_instance.smartmealtable.port}"
  description = "RDS database endpoint (host:port format)"
}

output "rds_host" {
  value       = aws_db_instance.smartmealtable.address
  description = "RDS database hostname only"
}

output "db_username" {
  value       = aws_db_instance.smartmealtable.username
  description = "RDS database master username"
  sensitive   = false
}

output "db_password" {
  value       = aws_db_instance.smartmealtable.password
  description = "RDS database master password"
  sensitive   = true
}

# CloudWatch 대시보드 생성
resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "smartmealtable-dashboard"

  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 8
        height = 6
        properties = {
          view    = "timeSeries"
          stacked = false
          metrics = [
            ["AWS/EC2", "CPUUtilization", "InstanceId", aws_instance.api.id],
            ["AWS/EC2", "CPUUtilization", "InstanceId", aws_instance.admin.id],
            ["AWS/EC2", "CPUUtilization", "InstanceId", aws_instance.batch.id]
          ]
          region = "ap-northeast-2"
          title  = "EC2 CPU 사용률 (%)"
          period = 300
          stat   = "Average"
          yAxis = {
            left = {
              min = 0
              max = 100
            }
          }
        }
      },
      {
        type   = "metric"
        x      = 8
        y      = 0
        width  = 8
        height = 6
        properties = {
          view    = "timeSeries"
          stacked = false
          metrics = [
            [
              "CWAgent",
              "mem_used_percent",
              "InstanceId", aws_instance.api.id,
              "InstanceType", aws_instance.api.instance_type
            ],
            [
              "CWAgent",
              "mem_used_percent",
              "InstanceId", aws_instance.admin.id,
              "InstanceType", aws_instance.admin.instance_type
            ],
            [
              "CWAgent",
              "mem_used_percent",
              "InstanceId", aws_instance.batch.id,
              "InstanceType", aws_instance.batch.instance_type
            ]
          ]
          region = "ap-northeast-2"
          title  = "EC2 메모리 사용률 (%)"
          period = 300
          stat   = "Average"
          yAxis = {
            left = {
              min = 0
              max = 100
            }
          }
        }
      },
      {
        type   = "metric"
        x      = 16
        y      = 0
        width  = 8
        height = 6
        properties = {
          view    = "timeSeries"
          stacked = false
          metrics = [
            ["AWS/EC2", "NetworkIn", "InstanceId", aws_instance.api.id],
            ["AWS/EC2", "NetworkOut", "InstanceId", aws_instance.api.id]
          ]
          region = "ap-northeast-2"
          title  = "EC2 네트워크 트래픽"
          period = 300
          stat   = "Average"
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 12
        height = 6
        properties = {
          view    = "timeSeries"
          stacked = false
          metrics = [
            ["AWS/RDS", "CPUUtilization", "DBInstanceIdentifier", aws_db_instance.smartmealtable.identifier]
          ]
          region = "ap-northeast-2"
          title  = "RDS CPU 사용률 (%)"
          period = 300
          stat   = "Average"
          yAxis = {
            left = {
              min = 0
              max = 100
            }
          }
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 6
        width  = 12
        height = 6
        properties = {
          view    = "timeSeries"
          stacked = false
          metrics = [
            ["AWS/RDS", "DatabaseConnections", "DBInstanceIdentifier", aws_db_instance.smartmealtable.identifier],
            ["AWS/RDS", "FreeableMemory", "DBInstanceIdentifier", aws_db_instance.smartmealtable.identifier],
            ["AWS/RDS", "FreeStorageSpace", "DBInstanceIdentifier", aws_db_instance.smartmealtable.identifier]
          ]
          region = "ap-northeast-2"
          title  = "RDS 상태"
          period = 300
          stat   = "Average"
        }
      }
    ]
  })
}

# EC2에 CloudWatch Agent 설치를 위한 IAM 정책 추가
resource "aws_iam_role_policy_attachment" "cloudwatch_agent" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

# API 서버용 Elastic IP
resource "aws_eip" "api" {
  instance = aws_instance.api.id
  domain   = "vpc"

  tags = {
    Name = "smartmealtable-api-eip"
  }
}

# Admin 서버용 Elastic IP  
resource "aws_eip" "admin" {
  instance = aws_instance.admin.id
  domain   = "vpc"

  tags = {
    Name = "smartmealtable-admin-eip"
  }
}

# 배치 서버용 Elastic IP (선택적)
resource "aws_eip" "batch" {
  instance = aws_instance.batch.id
  domain   = "vpc"

  tags = {
    Name = "smartmealtable-batch-eip"
  }
}

# 출력값 수정
output "api_instance_id" {
  value = aws_instance.api.id
}

output "admin_instance_id" {
  value = aws_instance.admin.id
}

output "batch_instance_id" {
  value = aws_instance.batch.id
}

output "api_url" {
  value = "http://${aws_eip.api.public_ip}:8080"
}

output "admin_url" {
  value = "http://${aws_eip.admin.public_ip}:8081"
}

output "grafana_url" {
  value = "http://${aws_eip.admin.public_ip}:3000"
}

output "prometheus_url" {
  value = "http://${aws_eip.admin.public_ip}:9090"
}

output "api_public_ip" {
  value = aws_eip.api.public_ip
}

output "admin_public_ip" {
  value = aws_eip.admin.public_ip
}

output "batch_public_ip" {
  value = aws_eip.batch.public_ip
}
