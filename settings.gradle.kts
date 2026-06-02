rootProject.name = "baezzal"

include("baezzal-application")

include("baezzal-config")
include("baezzal-config:jpa")
include("baezzal-config:mysql")
include("baezzal-config:redis")
include("baezzal-config:minio")
include("baezzal-config:fcm")
include("baezzal-config:swagger")
include("baezzal-curation")
include("baezzal-media")
include("baezzal-notification")
include("baezzal-platform")
include("baezzal-platform:cache")
include("baezzal-platform:image")
include("baezzal-platform:lock")
include("baezzal-platform:object-storage")
include("baezzal-platform:queue")
include("baezzal-platform:set")
include("baezzal-platform:messaging")
include("baezzal-platform:push")
include("baezzal-platform:web")

include("baezzal-config:jwt")
include("baezzal-config:oauth")

include("baezzal-platform:token")
