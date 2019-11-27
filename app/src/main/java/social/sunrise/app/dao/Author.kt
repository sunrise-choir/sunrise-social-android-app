package social.sunrise.app.dao

import com.sunrisechoir.graphql.AuthorProfileQuery
import social.sunrise.app.lib.AuthorRelationship
import social.sunrise.app.models.LiveAuthor

interface Author {
    fun get(query: () -> AuthorProfileQuery.Builder): LiveAuthor
    fun changeRelationship(relationship: AuthorRelationship, authorId: String)
    fun updateProfile(name: String, description: String, authorId: String)
}