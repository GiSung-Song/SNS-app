package com.outsta.sns.domain.block;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBlock is a Querydsl query type for Block
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBlock extends EntityPathBase<Block> {

    private static final long serialVersionUID = 1685081991L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBlock block = new QBlock("block");

    public final com.outsta.sns.domain.QBaseTimeEntity _super = new com.outsta.sns.domain.QBaseTimeEntity(this);

    public final com.outsta.sns.domain.member.entity.QMember blocked;

    public final com.outsta.sns.domain.member.entity.QMember blocker;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DatePath<java.time.LocalDate> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBlock(String variable) {
        this(Block.class, forVariable(variable), INITS);
    }

    public QBlock(Path<? extends Block> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBlock(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBlock(PathMetadata metadata, PathInits inits) {
        this(Block.class, metadata, inits);
    }

    public QBlock(Class<? extends Block> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.blocked = inits.isInitialized("blocked") ? new com.outsta.sns.domain.member.entity.QMember(forProperty("blocked")) : null;
        this.blocker = inits.isInitialized("blocker") ? new com.outsta.sns.domain.member.entity.QMember(forProperty("blocker")) : null;
    }

}

