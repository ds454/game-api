#! /bin/sh

# Pull the latest submodule updates
git submodule update --remote --merge

# mvn dependency:resolve
echo Dev Container started. Happy coding!