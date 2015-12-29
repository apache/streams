/*
root
 |-- contributors: string (nullable = true)
 |-- coordinates: string (nullable = true)
 |-- created_at: string (nullable = true)
 |-- entities: struct (nullable = true)
 |    |-- hashtags: array (nullable = true)
 |    |    |-- element: struct (containsNull = true)
 |    |    |    |-- indices: array (nullable = true)
 |    |    |    |    |-- element: long (containsNull = true)
 |    |    |    |-- text: string (nullable = true)
 |    |-- symbols: array (nullable = true)
 |    |    |-- element: string (containsNull = true)
 |    |-- urls: array (nullable = true)
 |    |    |-- element: struct (containsNull = true)
 |    |    |    |-- display_url: string (nullable = true)
 |    |    |    |-- expanded_url: string (nullable = true)
 |    |    |    |-- indices: array (nullable = true)
 |    |    |    |    |-- element: long (containsNull = true)
 |    |    |    |-- url: string (nullable = true)
 |    |-- user_mentions: array (nullable = true)
 |    |    |-- element: struct (containsNull = true)
 |    |    |    |-- id: long (nullable = true)
 |    |    |    |-- id_str: string (nullable = true)
 |    |    |    |-- indices: array (nullable = true)
 |    |    |    |    |-- element: long (containsNull = true)
 |    |    |    |-- name: string (nullable = true)
 |    |    |    |-- screen_name: string (nullable = true)
 |-- favorite_count: long (nullable = true)
 |-- favorited: boolean (nullable = true)
 |-- geo: string (nullable = true)
 |-- id: long (nullable = true)
 |-- id_str: string (nullable = true)
 |-- in_reply_to_screen_name: string (nullable = true)
 |-- in_reply_to_status_id: long (nullable = true)
 |-- in_reply_to_status_id_str: string (nullable = true)
 |-- in_reply_to_user_id: long (nullable = true)
 |-- in_reply_to_user_id_str: string (nullable = true)
 |-- lang: string (nullable = true)
 |-- place: struct (nullable = true)
 |    |-- attributes: struct (nullable = true)
 |    |-- bounding_box: struct (nullable = true)
 |    |    |-- coordinates: array (nullable = true)
 |    |    |    |-- element: array (containsNull = true)
 |    |    |    |    |-- element: array (containsNull = true)
 |    |    |    |    |    |-- element: double (containsNull = true)
 |    |    |-- type: string (nullable = true)
 |    |-- contained_within: array (nullable = true)
 |    |    |-- element: string (containsNull = true)
 |    |-- country: string (nullable = true)
 |    |-- country_code: string (nullable = true)
 |    |-- full_name: string (nullable = true)
 |    |-- id: string (nullable = true)
 |    |-- name: string (nullable = true)
 |    |-- place_type: string (nullable = true)
 |    |-- url: string (nullable = true)
 |-- possibly_sensitive: boolean (nullable = true)
 |-- retweet_count: long (nullable = true)
 |-- retweeted: boolean (nullable = true)
 |-- retweeted_status: struct (nullable = true)
 |    |-- contributors: string (nullable = true)
 |    |-- coordinates: string (nullable = true)
 |    |-- created_at: string (nullable = true)
 |    |-- entities: struct (nullable = true)
 |    |    |-- hashtags: array (nullable = true)
 |    |    |    |-- element: struct (containsNull = true)
 |    |    |    |    |-- indices: array (nullable = true)
 |    |    |    |    |    |-- element: long (containsNull = true)
 |    |    |    |    |-- text: string (nullable = true)
 |    |    |-- symbols: array (nullable = true)
 |    |    |    |-- element: string (containsNull = true)
 |    |    |-- urls: array (nullable = true)
 |    |    |    |-- element: struct (containsNull = true)
 |    |    |    |    |-- display_url: string (nullable = true)
 |    |    |    |    |-- expanded_url: string (nullable = true)
 |    |    |    |    |-- indices: array (nullable = true)
 |    |    |    |    |    |-- element: long (containsNull = true)
 |    |    |    |    |-- url: string (nullable = true)
 |    |    |-- user_mentions: array (nullable = true)
 |    |    |    |-- element: struct (containsNull = true)
 |    |    |    |    |-- id: long (nullable = true)
 |    |    |    |    |-- id_str: string (nullable = true)
 |    |    |    |    |-- indices: array (nullable = true)
 |    |    |    |    |    |-- element: long (containsNull = true)
 |    |    |    |    |-- name: string (nullable = true)
 |    |    |    |    |-- screen_name: string (nullable = true)
 |    |-- favorite_count: long (nullable = true)
 |    |-- favorited: boolean (nullable = true)
 |    |-- geo: string (nullable = true)
 |    |-- id: long (nullable = true)
 |    |-- id_str: string (nullable = true)
 |    |-- in_reply_to_screen_name: string (nullable = true)
 |    |-- in_reply_to_status_id: string (nullable = true)
 |    |-- in_reply_to_status_id_str: string (nullable = true)
 |    |-- in_reply_to_user_id: string (nullable = true)
 |    |-- in_reply_to_user_id_str: string (nullable = true)
 |    |-- lang: string (nullable = true)
 |    |-- place: struct (nullable = true)
 |    |    |-- attributes: struct (nullable = true)
 |    |    |-- bounding_box: struct (nullable = true)
 |    |    |    |-- coordinates: array (nullable = true)
 |    |    |    |    |-- element: array (containsNull = true)
 |    |    |    |    |    |-- element: array (containsNull = true)
 |    |    |    |    |    |    |-- element: double (containsNull = true)
 |    |    |    |-- type: string (nullable = true)
 |    |    |-- contained_within: array (nullable = true)
 |    |    |    |-- element: string (containsNull = true)
 |    |    |-- country: string (nullable = true)
 |    |    |-- country_code: string (nullable = true)
 |    |    |-- full_name: string (nullable = true)
 |    |    |-- id: string (nullable = true)
 |    |    |-- name: string (nullable = true)
 |    |    |-- place_type: string (nullable = true)
 |    |    |-- url: string (nullable = true)
 |    |-- possibly_sensitive: boolean (nullable = true)
 |    |-- retweet_count: long (nullable = true)
 |    |-- retweeted: boolean (nullable = true)
 |    |-- source: string (nullable = true)
 |    |-- text: string (nullable = true)
 |    |-- truncated: boolean (nullable = true)
 |    |-- user: struct (nullable = true)
 |    |    |-- contributors_enabled: boolean (nullable = true)
 |    |    |-- created_at: string (nullable = true)
 |    |    |-- default_profile: boolean (nullable = true)
 |    |    |-- default_profile_image: boolean (nullable = true)
 |    |    |-- description: string (nullable = true)
 |    |    |-- entities: struct (nullable = true)
 |    |    |    |-- description: struct (nullable = true)
 |    |    |    |    |-- urls: array (nullable = true)
 |    |    |    |    |    |-- element: struct (containsNull = true)
 |    |    |    |    |    |    |-- display_url: string (nullable = true)
 |    |    |    |    |    |    |-- expanded_url: string (nullable = true)
 |    |    |    |    |    |    |-- indices: array (nullable = true)
 |    |    |    |    |    |    |    |-- element: long (containsNull = true)
 |    |    |    |    |    |    |-- url: string (nullable = true)
 |    |    |    |-- url: struct (nullable = true)
 |    |    |    |    |-- urls: array (nullable = true)
 |    |    |    |    |    |-- element: struct (containsNull = true)
 |    |    |    |    |    |    |-- display_url: string (nullable = true)
 |    |    |    |    |    |    |-- expanded_url: string (nullable = true)
 |    |    |    |    |    |    |-- indices: array (nullable = true)
 |    |    |    |    |    |    |    |-- element: long (containsNull = true)
 |    |    |    |    |    |    |-- url: string (nullable = true)
 |    |    |-- favourites_count: long (nullable = true)
 |    |    |-- follow_request_sent: boolean (nullable = true)
 |    |    |-- followers_count: long (nullable = true)
 |    |    |-- following: boolean (nullable = true)
 |    |    |-- friends_count: long (nullable = true)
 |    |    |-- geo_enabled: boolean (nullable = true)
 |    |    |-- id: long (nullable = true)
 |    |    |-- id_str: string (nullable = true)
 |    |    |-- is_translation_enabled: boolean (nullable = true)
 |    |    |-- is_translator: boolean (nullable = true)
 |    |    |-- lang: string (nullable = true)
 |    |    |-- listed_count: long (nullable = true)
 |    |    |-- location: string (nullable = true)
 |    |    |-- name: string (nullable = true)
 |    |    |-- notifications: boolean (nullable = true)
 |    |    |-- profile_background_color: string (nullable = true)
 |    |    |-- profile_background_image_url: string (nullable = true)
 |    |    |-- profile_background_image_url_https: string (nullable = true)
 |    |    |-- profile_background_tile: boolean (nullable = true)
 |    |    |-- profile_banner_url: string (nullable = true)
 |    |    |-- profile_image_url: string (nullable = true)
 |    |    |-- profile_image_url_https: string (nullable = true)
 |    |    |-- profile_link_color: string (nullable = true)
 |    |    |-- profile_sidebar_border_color: string (nullable = true)
 |    |    |-- profile_sidebar_fill_color: string (nullable = true)
 |    |    |-- profile_text_color: string (nullable = true)
 |    |    |-- profile_use_background_image: boolean (nullable = true)
 |    |    |-- protected: boolean (nullable = true)
 |    |    |-- screen_name: string (nullable = true)
 |    |    |-- statuses_count: long (nullable = true)
 |    |    |-- time_zone: string (nullable = true)
 |    |    |-- url: string (nullable = true)
 |    |    |-- utc_offset: long (nullable = true)
 |    |    |-- verified: boolean (nullable = true)
 |-- source: string (nullable = true)
 |-- text: string (nullable = true)
 |-- truncated: boolean (nullable = true)
 |-- user: struct (nullable = true)
 |    |-- contributors_enabled: boolean (nullable = true)
 |    |-- created_at: string (nullable = true)
 |    |-- default_profile: boolean (nullable = true)
 |    |-- default_profile_image: boolean (nullable = true)
 |    |-- description: string (nullable = true)
 |    |-- entities: struct (nullable = true)
 |    |    |-- description: struct (nullable = true)
 |    |    |    |-- urls: array (nullable = true)
 |    |    |    |    |-- element: string (containsNull = true)
 |    |    |-- url: struct (nullable = true)
 |    |    |    |-- urls: array (nullable = true)
 |    |    |    |    |-- element: struct (containsNull = true)
 |    |    |    |    |    |-- display_url: string (nullable = true)
 |    |    |    |    |    |-- expanded_url: string (nullable = true)
 |    |    |    |    |    |-- indices: array (nullable = true)
 |    |    |    |    |    |    |-- element: long (containsNull = true)
 |    |    |    |    |    |-- url: string (nullable = true)
 |    |-- favourites_count: long (nullable = true)
 |    |-- follow_request_sent: boolean (nullable = true)
 |    |-- followers_count: long (nullable = true)
 |    |-- following: boolean (nullable = true)
 |    |-- friends_count: long (nullable = true)
 |    |-- geo_enabled: boolean (nullable = true)
 |    |-- id: long (nullable = true)
 |    |-- id_str: string (nullable = true)
 |    |-- is_translation_enabled: boolean (nullable = true)
 |    |-- is_translator: boolean (nullable = true)
 |    |-- lang: string (nullable = true)
 |    |-- listed_count: long (nullable = true)
 |    |-- location: string (nullable = true)
 |    |-- name: string (nullable = true)
 |    |-- notifications: boolean (nullable = true)
 |    |-- profile_background_color: string (nullable = true)
 |    |-- profile_background_image_url: string (nullable = true)
 |    |-- profile_background_image_url_https: string (nullable = true)
 |    |-- profile_background_tile: boolean (nullable = true)
 |    |-- profile_banner_url: string (nullable = true)
 |    |-- profile_image_url: string (nullable = true)
 |    |-- profile_image_url_https: string (nullable = true)
 |    |-- profile_link_color: string (nullable = true)
 |    |-- profile_sidebar_border_color: string (nullable = true)
 |    |-- profile_sidebar_fill_color: string (nullable = true)
 |    |-- profile_text_color: string (nullable = true)
 |    |-- profile_use_background_image: boolean (nullable = true)
 |    |-- protected: boolean (nullable = true)
 |    |-- screen_name: string (nullable = true)
 |    |-- statuses_count: long (nullable = true)
 |    |-- time_zone: string (nullable = true)
 |    |-- url: string (nullable = true)
 |    |-- utc_offset: long (nullable = true)
 |    |-- verified: boolean (nullable = true)
*/
create table tweet (
    created_at string,
    entities struct
    <
        hashtags: array
        <
            struct
            <
                text: string
            >
        >,
        urls: array
        <
            struct
            <
                expanded_url: string
            >
        >,
        user_mentions: array
        <
            struct
            <
                id_str: string
            >
        >
    >,
    in_reply_to_user_id_str: string,
    user: struct
    <
        id_str: string
    >,
    retweeted_status struct
    <
        entities struct
        <
            hashtags: array
            <
                struct
                <
                    text: string
                >
            >,
            urls: array
            <
                struct
                <
                    expanded_url: string
                >
            >,
            user_mentions: array
            <
                struct
                <
                    id_str: string
                >
            >
        >,
        in_reply_to_user_id_str: string,
        user: struct
        <
            id_str: string
        >
    >
)