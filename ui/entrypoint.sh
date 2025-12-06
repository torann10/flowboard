#!/bin/sh

FOLDER=/usr/share/nginx/html

# Function to replace strings in files recursively
replace_strings() {
  local folder="$1"
  local search_string="$2"
  local replace_string="$3"

  # Find all files in the folder recursively
  find "$folder" -type f -exec sed -i "s|$search_string|$replace_string|g" {} +
}

# Replace strings in files
replace_strings "$FOLDER" "__INGRESS_HOST_PLACEHOLDER__" "$INGRESS_HOST"
replace_strings "$FOLDER" "__KEYCLOAK_HOST_PLACEHOLDER__" "$KEYCLOAK_HOST"

# Execute the command passed as arguments (e.g., the main application)
exec /docker-entrypoint.sh "$@"
