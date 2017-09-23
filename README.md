A Java program that fetches statuses for Manhattan-bound L trains arriving at
Bedford Avenue and a C++ program that displays the train times on a 64x32 RGB
LED matrix. Intended to be run on Raspberry Pi 2.0.

Also includes a server for answering Google Assistant queries. See
[assistant/README.md](assistant/README.md).

![Demo photo](https://raw.githubusercontent.com/aj-michael/mta/master/img/demo.jpg)

## Setup

Add your MTA API key to `config.properties` and run `mkfifo /tmp/mtafifo`.

## Server

```
bazel run //java/net/ajmichael/mta
```

## Client

```
cd client && make && sudo ./mta
```

## License

GPLv2
