#!/bin/bash

set -eu

# copy tests for `public` modifier
perl -p -e 's/run java-test-transform\n//' ./compatibility/java/public.spt > ./codecompletion/public.spt

sed -i '' -e 's/compatibility\/java/code-completion/g' ./codecompletion/public.spt
sed -i '' -e 's/public var x/[[public]] var x/g' ./codecompletion/public.spt
sed -i '' -e 's/"Test Java Compatibility"/"Test Complete To Current" on #1/g' ./codecompletion/public.spt

# copy tests for `public` => `protected internal` modifier
cp ./codecompletion/public.spt ./codecompletion/protected-internal.spt

sed -i '' -e 's/test public/test protected internal/g' ./codecompletion/protected-internal.spt
sed -i '' -e 's/\[\[public\]\] var x/[[protected internal]] var x/g' ./codecompletion/protected-internal.spt
sed -i '' -e 's/\/public/\/protected-internal/g' ./codecompletion/protected-internal.spt

# copy tests for `public` => `protected` modifier
cp ./codecompletion/public.spt ./codecompletion/protected.spt

sed -i '' -e 's/test public/test protected/g' ./codecompletion/protected.spt
sed -i '' -e 's/\[\[public\]\] var x/[[protected]] var x/g' ./codecompletion/protected.spt
sed -i '' -e 's/\/public/\/protected/g' ./codecompletion/protected.spt


# copy tests for `public` => `internal` modifier
cp ./codecompletion/public.spt ./codecompletion/internal.spt

sed -i '' -e 's/test public/test internal/g' ./codecompletion/internal.spt
sed -i '' -e 's/\[\[public\]\] var x/[[internal]] var x/g' ./codecompletion/internal.spt
sed -i '' -e 's/\/public/\/internal/g' ./codecompletion/internal.spt


# copy tests for `public` => `private protected` modifier
cp ./codecompletion/public.spt ./codecompletion/private-protected.spt

sed -i '' -e 's/test public/test private protected/g' ./codecompletion/private-protected.spt
sed -i '' -e 's/\[\[public\]\] var x/[[private protected]] var x/g' ./codecompletion/private-protected.spt
sed -i '' -e 's/\/public/\/private-protected/g' ./codecompletion/private-protected.spt


# copy tests for `public` => `private` modifier
cp ./codecompletion/public.spt ./codecompletion/private.spt

sed -i '' -e 's/test public/test private/g' ./codecompletion/private.spt
sed -i '' -e 's/\[\[public\]\] var x/[[private]] var x/g' ./codecompletion/private.spt
sed -i '' -e 's/\/public/\/private/g' ./codecompletion/private.spt
