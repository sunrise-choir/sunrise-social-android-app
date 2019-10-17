package nz.scuttlebutt.android_go.dao

import com.sunrisechoir.graphql.AuthorProfileQuery
import nz.scuttlebutt.android_go.lib.AuthorRelationship
import nz.scuttlebutt.android_go.models.LiveAuthor

interface Author {
    fun get(query: () -> AuthorProfileQuery.Builder): LiveAuthor
    fun changeRelationship(relationship: AuthorRelationship, authorId: String)
}