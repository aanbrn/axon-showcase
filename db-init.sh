#!/bin/bash

createuser -d showcase \
  && createdb -E UTF-8 -l en_US.UTF-8 -T template0 -U showcase showcase-events
