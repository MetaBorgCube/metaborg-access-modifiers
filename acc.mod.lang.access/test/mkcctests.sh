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



### Copy Rust tests

# Rust public
cp ./compatibility/rust/public.spt ./codecompletion/mods-public.spt

sed -i '' -e 's/\/rust\/public/\/code-completion\/mods-public/g' ./codecompletion/mods-public.spt
sed -i '' -e 's/test public/test mods-public/g' ./codecompletion/mods-public.spt
sed -i '' -e 's/"Test Rust Compatibility"/"Test Complete To Current" on \#1/g' ./codecompletion/mods-public.spt

# Rust internal
cp ./compatibility/rust/internal.spt ./codecompletion/mods-internal.spt

sed -i '' -e 's/\/rust\/internal/\/code-completion\/mods-internal/g' ./codecompletion/mods-internal.spt
sed -i '' -e 's/test internal/test mods-internal/g' ./codecompletion/mods-internal.spt
sed -i '' -e 's/"Test Rust Compatibility"/"Test Complete To Current" on \#1/g' ./codecompletion/mods-internal.spt


# Rust internal-self
cp ./compatibility/rust/internal-self.spt ./codecompletion/mods-internal-self.spt

sed -i '' -e 's/\/rust\/internal/\/code-completion\/mods-internal/g' ./codecompletion/mods-internal-self.spt
sed -i '' -e 's/test internal/test mods-internal/g' ./codecompletion/mods-internal-self.spt
sed -i '' -e 's/"Test Rust Compatibility"/"Test Complete To Current" on \#1/g' ./codecompletion/mods-internal-self.spt

# Rust internal-parent
cp ./compatibility/rust/internal-parent.spt ./codecompletion/mods-internal-parent.spt

sed -i '' -e 's/\/rust\/internal/\/code-completion\/mods-internal/g' ./codecompletion/mods-internal-parent.spt
sed -i '' -e 's/test internal/test mods-internal/g' ./codecompletion/mods-internal-parent.spt
sed -i '' -e 's/"Test Rust Compatibility"/"Test Complete To Current" on \#1/g' ./codecompletion/mods-internal-parent.spt

# Rust internal-grandparent
cp ./compatibility/rust/internal-grandparent.spt ./codecompletion/mods-internal-grandparent.spt

sed -i '' -e 's/\/rust\/internal/\/code-completion\/mods-internal/g' ./codecompletion/mods-internal-grandparent.spt
sed -i '' -e 's/test internal/test mods-internal/g' ./codecompletion/mods-internal-grandparent.spt
sed -i '' -e 's/"Test Rust Compatibility"/"Test Complete To Current" on \#1/g' ./codecompletion/mods-internal-grandparent.spt

# Rust internal-grandgrandparent
cp ./compatibility/rust/internal-grandgrandparent.spt ./codecompletion/mods-internal-grandgrandparent.spt

sed -i '' -e 's/\/rust\/internal/\/code-completion\/mods-internal/g' ./codecompletion/mods-internal-grandgrandparent.spt
sed -i '' -e 's/test internal/test mods-internal/g' ./codecompletion/mods-internal-grandgrandparent.spt
sed -i '' -e 's/"Test Rust Compatibility"/"Test Complete To Current" on \#1/g' ./codecompletion/mods-internal-grandgrandparent.spt
