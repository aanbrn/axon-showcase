#!/bin/bash

createuser -d showcase \
  && createdb -U showcase -E UTF8 showcase-events \
  && createdb -U showcase -E UTF8 showcase-sagas
