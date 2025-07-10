package com.outsta.sns.domain.profile;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.outsta.sns.domain.profile.entity.ProfileImage;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberProfileImage is a Querydsl query type for MemberProfileImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberProfileImage extends EntityPathBase<ProfileImage> {

    private static final long serialVersionUID = 1709067350L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberProfileImage memberProfileImage = new QMemberProfileImage("memberProfileImage");

    public final com.outsta.sns.domain.QBaseTimeEntity _super = new com.outsta.sns.domain.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DatePath<java.time.LocalDate> deletedAt = _super.deletedAt;

    public final StringPath fileName = createString("fileName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final com.outsta.sns.domain.member.entity.QMember member;

    public final StringPath originName = createString("originName");

    public final BooleanPath represent = createBoolean("represent");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMemberProfileImage(String variable) {
        this(ProfileImage.class, forVariable(variable), INITS);
    }

    public QMemberProfileImage(Path<? extends ProfileImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberProfileImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberProfileImage(PathMetadata metadata, PathInits inits) {
        this(ProfileImage.class, metadata, inits);
    }

    public QMemberProfileImage(Class<? extends ProfileImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.outsta.sns.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

