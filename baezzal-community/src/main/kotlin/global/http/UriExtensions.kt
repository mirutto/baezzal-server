package global.http

import org.springframework.web.util.UriComponentsBuilder

fun appendQueryParams(
    uri: String,
    vararg param: Pair<String, String>,
): String =
    UriComponentsBuilder
        .fromUriString(uri)
        .apply {
            param.forEach { (key, value) ->
                queryParam(key, value)
            }
        }.build()
        .encode()
        .toUriString()
