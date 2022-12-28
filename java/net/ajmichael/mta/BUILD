java_binary(
    name = "mta",
    main_class = "net.ajmichael.mta.Main",
    srcs = ["Main.java"],
    deps = [":lib"],
)

java_library(
    name = "lib",
    srcs = [
        "ApiMtaInfoLookup.java",
        "DatamineLookup.java",
        "LTrainLookup.java"
    ],
    deps = ["//third_party/java/com/google/transit/realtime:gtfs_realtime"],
    resources = ["//:config.properties"],
)