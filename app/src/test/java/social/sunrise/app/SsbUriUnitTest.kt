package social.sunrise.app

import org.junit.Assert.assertEquals
import org.junit.Test
import social.sunrise.app.utils.SsbUri


class SsbUriUnitTest {
    @Test
    fun ssbAuthorRefToUri() {
        val refMsgLink = "@U5GvOKP/YUza9k53DSXxT0mk3PIrnyAmessvNfZl5E0=.ed25519"
        val uri = SsbUri.fromSigilLink(refMsgLink)
        assertEquals(SsbUri.Companion.UriType.feed, uri.type)
        assertEquals("U5GvOKP/YUza9k53DSXxT0mk3PIrnyAmessvNfZl5E0=", uri.key)
        assertEquals("ed25519", uri.keyType)
        assertEquals(
            "ssb:feed:ed25519:U5GvOKP/YUza9k53DSXxT0mk3PIrnyAmessvNfZl5E0=",
            uri.toUriString()
        )

        assertEquals(SsbUri.fromSsbUriLink(uri.toUriString()), uri)

        assert(SsbUri.isSsbRef(refMsgLink))
        assert(SsbUri.isSsbUri(uri.toUriString()))
        assertEquals(SsbUri.toSigilLink(uri.toUriString()), refMsgLink)
        assert(uri.isFeed())

        val ref = uri.toSigilLink()
        assertEquals(ref, refMsgLink)
    }

    @Test
    fun ssbMessageRefToUri() {
        val refMsgLink = "%U5GvOKP/YUza9k53DSXxT0mk3PIrnyAmessvNfZl5E0=.sha256"
        val uri = SsbUri.fromSigilLink(refMsgLink)
        assertEquals(SsbUri.Companion.UriType.message, uri.type)
        assertEquals("U5GvOKP/YUza9k53DSXxT0mk3PIrnyAmessvNfZl5E0=", uri.key)
        assertEquals("sha256", uri.keyType)
        assert(uri.isMessage())

        val ref = uri.toSigilLink()
        assertEquals(ref, refMsgLink)

    }

    @Test
    fun ssbBlobRefToUri() {
        val refMsgLink = "&U5GvOKP/YUza9k53DSXxT0mk3PIrnyAmessvNfZl5E0=.sha256"
        val uri = SsbUri.fromSigilLink(refMsgLink)
        assertEquals(SsbUri.Companion.UriType.blob, uri.type)
        assertEquals("U5GvOKP/YUza9k53DSXxT0mk3PIrnyAmessvNfZl5E0=", uri.key)
        assertEquals("sha256", uri.keyType)
        assert(uri.isBlob())

        val ref = uri.toSigilLink()
        assertEquals(ref, refMsgLink)
    }

}