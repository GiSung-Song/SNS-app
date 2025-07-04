package com.outsta.sns.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = 1082600822L;

    public static final QMember member = new QMember("member1");

    public final com.outsta.sns.domain.QBaseTimeEntity _super = new com.outsta.sns.domain.QBaseTimeEntity(this);

    public final EnumPath<com.outsta.sns.domain.enums.Activation> activation = createEnum("activation", com.outsta.sns.domain.enums.Activation.class);

    public final DatePath<java.time.LocalDate> birth = createDate("birth", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DatePath<java.time.LocalDate> deletedAt = _super.deletedAt;

    public final StringPath email = createString("email");

    public final EnumPath<com.outsta.sns.domain.enums.Gender> gender = createEnum("gender", com.outsta.sns.domain.enums.Gender.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DatePath<java.time.LocalDate> lastStoppedDate = createDate("lastStoppedDate", java.time.LocalDate.class);

    public final StringPath name = createString("name");

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final EnumPath<com.outsta.sns.domain.enums.Role> role = createEnum("role", com.outsta.sns.domain.enums.Role.class);

    public final NumberPath<Integer> stoppedCount = createNumber("stoppedCount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final EnumPath<com.outsta.sns.domain.enums.Visibility> visibility = createEnum("visibility", com.outsta.sns.domain.enums.Visibility.class);

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}

