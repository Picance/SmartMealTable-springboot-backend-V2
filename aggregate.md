# Aggregate 분석 문서

| 애그리거트 (Aggregate) | 애그리거트 루트 (Aggregate Root) | 포함된 객체 (Entities / VOs) |
| :--- | :--- | :--- |
| **회원 (Member)** | `Member` | `MemberAuthentication`, `SocialAccount`, `AddressHistory`, `PolicyAgreement`, `Preference`, `Favorite` |
| **가게 (Store)** | `Store` | `Food`, `Seller` |
| **지출 (Expenditure)** | `Expenditure` | `ExpenditureItem` |
| **장바구니 (Cart)** | `Cart` | `CartItem` |
| **일일 예산 (DailyBudget)** | `DailyBudget` | `MealBudget` |
| **월별 예산 (MonthlyBudget)** | `MonthlyBudget` | - |
| **카테고리 (Category)** | `Category` | - |
| **그룹 (Group)** | `Group` | - |
| **약관 (Policy)** | `Policy` | - |