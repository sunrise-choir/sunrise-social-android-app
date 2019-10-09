package nz.scuttlebutt.android_go.utils

data class SsbUri(val type: UriType, val key: String, val keyType: String) {

    fun toUriString(): String {
        return "$scheme:${type.name}:$keyType:$key"
    }

    fun toSigilLink(): String {
        return "${type.sigil}${key}.${keyType}"
    }

    fun isMessage(): Boolean {
        return type == UriType.message
    }

    fun isFeed(): Boolean {
        return type == UriType.feed
    }

    fun isChannel(): Boolean {
        return type == UriType.channel
    }

    fun isBlob(): Boolean {
        return type == UriType.blob
    }

    companion object {

        enum class UriType(val sigil: String) {
            message("%"),
            feed("@"),
            blob("&"),
            channel("#")
        }

        private val linkRegex = Regex("^([@%&])([A-Za-z0-9/+]{43}=)\\.([\\w\\d]+\$)")
        private val uriRegex = Regex("^ssb:(\\w+):(\\w+):(.+\$)")
        private const val scheme = "ssb"

        fun isSsbRef(link: String): Boolean {
            return linkRegex.matches(link)
        }

        fun isSsbUri(uriString: String): Boolean {
            return uriRegex.matches(uriString)
        }

        fun fromSigilLink(link: String): SsbUri {
            val split = linkRegex.matchEntire(link)
            val (_, sigil, key, keyType) = split!!.groupValues

            return SsbUri(authorityFromSigil(sigil), key = key, keyType = keyType)
        }

        fun fromSsbUriLink(link: String): SsbUri {
            val split = uriRegex.matchEntire(link)
            val (_, authorityString, keyType, key) = split!!.groupValues

            val authority = UriType.valueOf(authorityString)

            return SsbUri(authority, key = key, keyType = keyType)
        }

        fun toSigilLink(uriString: String): String {
            val split = uriRegex.matchEntire(uriString)
            val (_, authority, keyType, key) = split!!.groupValues
            return "${sigilFromAuthority(authority)}${key}.${keyType}"
        }

        private fun authorityFromSigil(sigil: String): UriType {
            return when (sigil) {
                "&" -> UriType.blob
                "@" -> UriType.feed
                "%" -> UriType.message
                "#" -> UriType.channel
                else -> {
                    throw Error("Unknown sigil type: $sigil")
                }
            }
        }

        private fun sigilFromAuthority(authority: String): String {
            return UriType.valueOf(authority).sigil
        }
    }
}