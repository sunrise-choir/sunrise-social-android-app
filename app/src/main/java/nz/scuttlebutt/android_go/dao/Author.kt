package nz.scuttlebutt.android_go.dao

import com.sunrisechoir.graphql.AuthorProfileQuery
import nz.scuttlebutt.android_go.models.LiveAuthor

enum class AuthorRelationship(postId: String) {
    FOLLOW(""),
    UNFOLLOW(""),
    BLOCK(""),
    UNBLOCK(""),
}

interface Author {
    fun get(query: () -> AuthorProfileQuery.Builder): LiveAuthor
    fun changeRelationship(relationship: AuthorRelationship)
}