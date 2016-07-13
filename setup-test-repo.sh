#!/bin/sh
mkdir -p repo/test
cd repo/test
git clone --bare https://github.com/ohucode/empty-repo empty
git clone --bare https://github.com/ohucode/fixture-repo fixture
git clone --bare https://github.com/ohucode/fixture-repo private
