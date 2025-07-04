package com.outsta.sns.domain.tag;

import com.outsta.sns.domain.post.Post;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_tag",
    uniqueConstraints = @UniqueConstraint(name = "unique_post_tag", columnNames = {"post_id", "tag_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}