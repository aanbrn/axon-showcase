#!/bin/bash

dropdb --if-exists showcase-events \
  && dropdb --if-exists showcase-sagas \
  && dropuser --if-exists showcase
