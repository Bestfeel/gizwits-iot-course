#!/usr/bin/env bash


mvn  clean compile  package  -Dmaven.test.skip=true

docker build -t  daocloud.io/gizwits2015/log-collection     .