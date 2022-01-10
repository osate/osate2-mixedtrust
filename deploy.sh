set -x

FROMDIR=org.osate.analysis.mixedtrust.repository/target/repository
TODIR=/var/www/html/p2/osate2-mixedtrust

rm -fr ${TODIR}
mkdir -p ${TODIR}
cp --recursive ${FROMDIR}/* ${TODIR}/.
