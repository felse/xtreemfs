#!/bin/bash

# Path is relative to the directory path of this script, usually /packaging/
xtreemfs_version_management_files="../java/foundation/src/org/xtreemfs/foundation/VersionManagement.java:RELEASE_VERSION ../cpp/include/libxtreemfs/version_management.h:XTREEMFS_VERSION_STRING"

xtreemfs_release_names="
1.3|Tasty Tartlet
"

function display_usage() {
  cat <<EOF
Usage: ./set-version.sh [option] where option may be

-i            Prompt for new version from stdin. If found, the release name will be appended in parentheses.
-n <version>  Set <version> to new version string. If found, the release name will be appended in parentheses.
-s            Generate version string from SVN revision obtained from "svn info"

EOF
}

function update_version() {
  version_string="$1"
  add_svn_revision_only="$2"

  for entry in $xtreemfs_version_management_files
  do
    file=$(echo "$entry" | cut -d: -f1)
    file="${path_to_packaging_directory}/${file}"
    keyword=$(echo "$entry" | cut -d: -f2)

    # DEBUG:
    #svn revert "$file" &>> /dev/null

    # Check first if key word exists in version file
    grep "$keyword" "$file" >> /dev/null
    if [ $? -ne 0 ]
    then
      echo "No key word \"$keyword\" found in file $file, so the version could not get adjusted there."
      continue
    fi

    # Retrieve current version
    current_version=$(grep "$keyword" "$file" | grep -oE "\".*\"" | tr -d '"')
    if [ -n "$add_svn_revision_only" ]
    then
      current_version=$(echo "$current_version" | sed -r -e "s/^.*-?trunk/trunk/")
      echo "$current_version" "$version_string"
    fi

    # Change version
    if [ "$current_version" != "$version_string" ]
    then
      if [ -n "$add_svn_revision_only" ]
      then
        #echo "Replacing trunk with new version string: $version_string"
        sed -i -e "s/$current_version\"/$version_string\"/" $file        
      else
        #echo "Replacing current version: $current_version with new version string: $version_string"
        sed -i -e "s/\"$current_version\"/\"$version_string\"/" $file
      fi
      if [ $? -ne 0 ]
      then
        echo "Failed to run sed on file: $file" 
      fi
#    else
#      echo "No need to replace: $current_version with: $version_string"
    fi
  done
}

function find_releasename() {
  local version="$1"
  
  # Strip last version number until there is no number left
  release_name_found=0
  while [ $release_name_found -eq 0 ]
  do
    # Abort version detection for single version numbers (e.g. "1") as it would match every version
    numbers=$(echo "$version" | awk -F. '{ print NF }')
    if [ $numbers -lt 2 ]
    then
      break
    fi

    IFS='
'
    for line in $xtreemfs_release_names
    do
      # Skip empty lines
      if [ -z "$line" ]
      then
        continue
      fi

      number=$(echo "$line" | cut -f1 -d\|)
      release=$(echo "$line" | cut -f2 -d\|)

      echo "$number" | grep -E "^$version$" >> /dev/null
      if [ $? -eq 0 ]
      then
        echo $release
        release_name_found=1
        break
      fi
    done
    unset IFS

    version=`echo "$version" | sed -r -e "s/\.?[0-9]$//"`
  done
}

function check_version_format() {
  local version="$1"

  echo "$version" | grep -E "^[1-9]\.([0-9]|[1-9][0-9]+)\.([0-9]|[1-9][0-9]+)(\.([89][0-9]))?$" >> /dev/null
  if [ $? -ne 0 ]
  then
    echo "Invalid version entered: $version"
    echo
    display_usage
    exit 2
  fi
}

# main()
if [ -z "$1" ]
then
  display_usage
  exit 1
fi

path_to_packaging_directory=$(dirname "$0")

case "$1" in
  -i)
    while [ -z "$version" ]
    do
      cat << EOF
Please enter the new version.

  Format: 1.x.y(.z)

x stands for feature and y for stable releases.
z is optional and may be in the following ranges:
.80+ = beta releases
.90+ = release candidates
EOF
      read version
    done

    check_version_format "$version"
    releasename=$(find_releasename "$2")
    if [ -z "$releasename" ]
    then
      version_string=$version
    else
      version_string="$version ($releasename)"
    fi
    update_version "$version_string"
    echo "Changed version string to: $version_string"
    ;;
  -n)
    if [ -z "$2" ]
    then
      echo "No version entered as second parameter."
      echo
      display_usage
      exit 1
    fi

    version="$2"
    check_version_format "$version"
    releasename=$(find_releasename "$2")
    if [ -z "$releasename" ]
    then
      version_string=$version
    else
      version_string="$version ($releasename)"
    fi
    update_version "$version_string"
    echo "OK $version $releasename"
    ;;
  -s)
    svn_root="${path_to_packaging_directory}/.."
    svn info "$svn_root" &>/dev/null
    if [ $? -ne 0 ]
    then
      # silently stop here if no 'svn' is installed to retrieve branch and revision of the sources
      exit 0
    fi

    revision=$(svn info "$svn_root" | grep ^Revision | awk '{ print $2 }')
    trunk_or_branch=$(svn info "$svn_root" | grep ^URL | sed -r -e "s/URL:[ \t]+http(s)?:\/\/xtreemfs.googlecode.com\/svn\/(branches\/)?//")

    if [ "$trunk_or_branch" = "trunk" ]
    then
      version_string="trunk-svn-r${revision}"
      update_version "$version_string" "replace-trunk-only"
    else
      version_version="${trunk_or_branch} svn-r${revision}"
      update_version "$version_string"
    fi
    ;;
  *)
    echo "Unknown option: $1"
    echo
    display_usage
    exit 1
    ;;
esac