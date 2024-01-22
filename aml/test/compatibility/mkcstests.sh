#!/bin/bash

set -eu

# copy tests for `public` modifier
cp ./java/public.spt ./csharp/public.spt

sed -i '' -e 's/java/csharp/g' ./csharp/public.spt
sed -i '' -e 's/Test Java/Test C#/g' ./csharp/public.spt

# copy tests for `protected` => `protected internal` modifier
cp ./java/protected.spt ./csharp/protected-internal.spt

sed -i '' -e 's/java/csharp/g' ./csharp/protected-internal.spt
sed -i '' -e 's/test protected/test protected internal/g' ./csharp/protected-internal.spt
sed -i '' -e 's/Test Java/Test C#/g' ./csharp/protected-internal.spt

# create tests for `protected` modifier
cp ./java/protected.spt ./csharp/protected.spt

sed -i '' -e 's/java/csharp/g' ./csharp/protected.spt
sed -i '' -e 's/internal //g' ./csharp/protected.spt
sed -i '' -e 's/Test Java/Test C#/g' ./csharp/protected.spt

# create tests for `private protected` modifier
cp ./csharp/protected.spt ./csharp/private-protected.spt

sed -i '' -e 's/protected/private protected/g' ./csharp/private-protected.spt

# copy tests for `internal` modifier
cp ./java/internal.spt ./csharp/internal.spt

sed -i '' -e 's/java/csharp/g' ./csharp/internal.spt
sed -i '' -e 's/Test Java/Test C#/g' ./csharp/internal.spt

# copy tests for `private` modifier
cp ./java/private.spt ./csharp/private.spt

sed -i '' -e 's/java/csharp/g' ./csharp/private.spt
sed -i '' -e 's/Test Java/Test C#/g' ./csharp/private.spt
