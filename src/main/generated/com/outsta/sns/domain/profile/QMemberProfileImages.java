package com.outsta.sns.domain.profile;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberProfileImages is a Querydsl query type for MemberProfileImages
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberProfileImages extends EntityPathBase<MemberProfileImages> {

    private static final long serialVersionUID = 1441480413L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberProfileImages memberProfileImages = new QMemberProfileImages("memberProfileImages");

    public final com.outsta.sns.domain.QBaseTimeEntity _super = new com.outsta.sns.domain.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DatePath<java.time.LocalDate> deletedAt = _super.deletedAt;

    public final StringPath fileName = createString("fileName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final BooleanPath isRepresent = createBoolean("isRepresent");

    public final com.outsta.sns.domain.member.entity.QMember member;

    public final StringPath originName = createString("originName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMemberProfileImages(String variable) {
        this(MemberProfileImages.class, forVariable(variable), INITS);
    }

    public QMemberProfileImages(Path<? extends MemberProfileImages> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberProfileImages(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberProfileImages(PathMetadata metadata, PathInits inits) {
        this(MemberProfileImages.class, metadata, inits);
    }

    public QMemberProfileImages(Class<? extends MemberProfileImages> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.outsta.sns.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

