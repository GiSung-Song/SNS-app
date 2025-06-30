package com.outsta.sns.domain.reply;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReply is a Querydsl query type for Reply
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReply extends EntityPathBase<Reply> {

    private static final long serialVersionUID = 626913921L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReply reply1 = new QReply("reply1");

    public final com.outsta.sns.domain.QBaseTimeEntity _super = new com.outsta.sns.domain.QBaseTimeEntity(this);

    public final StringPath comment = createString("comment");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DatePath<java.time.LocalDate> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.outsta.sns.domain.member.entity.QMember member;

    public final com.outsta.sns.domain.post.QPost post;

    public final QReply reply;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QReply(String variable) {
        this(Reply.class, forVariable(variable), INITS);
    }

    public QReply(Path<? extends Reply> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReply(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReply(PathMetadata metadata, PathInits inits) {
        this(Reply.class, metadata, inits);
    }

    public QReply(Class<? extends Reply> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.outsta.sns.domain.member.entity.QMember(forProperty("member")) : null;
        this.post = inits.isInitialized("post") ? new com.outsta.sns.domain.post.QPost(forProperty("post")) : null;
        this.reply = inits.isInitialized("reply") ? new QReply(forProperty("reply"), inits.get("reply")) : null;
    }

}

