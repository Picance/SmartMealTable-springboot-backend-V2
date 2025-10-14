package com.stdev.smartmealtable.storage.db.settings.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotificationSettingsJpaEntity is a Querydsl query type for NotificationSettingsJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationSettingsJpaEntity extends EntityPathBase<NotificationSettingsJpaEntity> {

    private static final long serialVersionUID = 1487941464L;

    public static final QNotificationSettingsJpaEntity notificationSettingsJpaEntity = new QNotificationSettingsJpaEntity("notificationSettingsJpaEntity");

    public final BooleanPath budgetAlertEnabled = createBoolean("budgetAlertEnabled");

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final NumberPath<Long> notificationSettingsId = createNumber("notificationSettingsId", Long.class);

    public final BooleanPath passwordExpiryAlertEnabled = createBoolean("passwordExpiryAlertEnabled");

    public final BooleanPath pushEnabled = createBoolean("pushEnabled");

    public final BooleanPath recommendationEnabled = createBoolean("recommendationEnabled");

    public final BooleanPath storeNoticeEnabled = createBoolean("storeNoticeEnabled");

    public QNotificationSettingsJpaEntity(String variable) {
        super(NotificationSettingsJpaEntity.class, forVariable(variable));
    }

    public QNotificationSettingsJpaEntity(Path<? extends NotificationSettingsJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotificationSettingsJpaEntity(PathMetadata metadata) {
        super(NotificationSettingsJpaEntity.class, metadata);
    }

}

