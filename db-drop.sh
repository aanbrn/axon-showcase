#!/bin/bash

dropdb --if-exists showcase-events \
  && dropuser --if-exists showcase
