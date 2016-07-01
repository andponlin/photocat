#!/usr/bin/python

# =====================================
# Copyright 2016, Andrew Lindesay. All Rights Reserved.
# Distributed under the terms of the MIT License.
#
# Authors:
#		Andrew Lindesay, apl@lindesay.co.nz
# =====================================

# This script is used to create a new tagged version of the software and to bump the 'current' version up one.
# This is essentially the 'release process'.

import os.path
import sys
import re
import xml.etree.ElementTree as etree
import common
import subprocess


# ----------------
# PARSE TOP-LEVEL POM AND GET MODULE NAMES

if not os.path.isfile("pom.xml"):
    print "the 'pom.xml' file should be accessible in the present working directory"
    sys.exit(1)

rootPomTree = etree.parse("pom.xml")

if not rootPomTree:
    print "the 'pom.xml' should be accessible in the present working directory"
    sys.exit(1)

if common.pomextractartifactid(rootPomTree) != "photocat":
    print "the top level pom should have the 'photocat' artifactId"
    sys.exit(1)

rootPomModuleNames = common.scanmodules()

# ----------------
# GET VERSION

rootPomCurrentVersion = common.pomextractversion(rootPomTree)
rootPomCurrentVersionMatch = re.match("^([1-9][0-9]*\.[0-9]+\.)([1-9][0-9]*)-SNAPSHOT$", rootPomCurrentVersion)

if not rootPomCurrentVersionMatch:
    print "the current root pom version is not a valid snapshot version; " + rootPomCurrentVersion
    sys.exit(1)

rootPomCurrentVersionPrefix = rootPomCurrentVersionMatch.group(1)
rootPomCurrentVersionSuffix = rootPomCurrentVersionMatch.group(2)

print "top-level version; " + rootPomCurrentVersion

releaseVersion = rootPomCurrentVersionPrefix + rootPomCurrentVersionSuffix
futureVersion = rootPomCurrentVersionPrefix + str(int(rootPomCurrentVersionSuffix) + 1) + "-SNAPSHOT"

# ----------------
# CHECK CURRENT CONSISTENCY

# This will make sure that all of the modules have the same version.

print "will check version consistency"

for m in rootPomModuleNames:
    common.ensurecurrentversionconsistencyformodule(m, rootPomCurrentVersion)

# ----------------
# RESET THE VERSIONS SANS THE SNAPSHOT

if 0 == subprocess.call(["mvn", "-q", "versions:set", "-DnewVersion=" + releaseVersion, "-DgenerateBackupPoms=false"]):
    print "versions:set to "+releaseVersion
else:
    print "failed version:set to " + releaseVersion
    sys.exit(1)

# ----------------
# ADD POMS TO GIT, COMMIT AND TAG

print "will git-add pom files"
common.gitaddpomformodule(None)

for m in rootPomModuleNames:
    common.gitaddpomformodule(m)

if 0 == subprocess.call(["git", "commit", "-m", "version " + releaseVersion]):
    print "git committed 'version " + releaseVersion + "'"
else:
    print "failed to git commit"
    sys.exit(1)

if 0 == subprocess.call(["git", "tag", "-a", "photocat-" + releaseVersion, "-m", "photocat-" + releaseVersion]):
    print "git tagged 'photocat-" + releaseVersion + "'"
else:
    print "failed to git tag"
    sys.exit(1)

# ----------------
# UPDATE ALL POMS TO NEW SNAPSHOT

if 0 == subprocess.call(["mvn", "-q", "versions:set", "-DnewVersion=" + futureVersion, "-DgenerateBackupPoms=false"]):
    print "versions:set to "+futureVersion
else:
    print "failed version:set to " + futureVersion
    sys.exit(1)

# ----------------
# ADD POMS TO GIT, COMMIT

print "will git-add pom files"
common.gitaddpomformodule(None)

for m in rootPomModuleNames:
    common.gitaddpomformodule(m)

if 0 == subprocess.call(["git", "commit", "-m", "version " + futureVersion]):
    print "git committed 'version " + futureVersion + "'"
else:
    print "failed to git commit"
    sys.exit(1)

# ----------------
# REMINDER AT THE END TO PUSH

print "---------------"
print "to complete the release; git push && git push --tags"