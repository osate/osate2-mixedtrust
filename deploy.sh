set -x

TODIR=/var/www/html/p2/ocarina
rm -fr ${TODIR}
mkdir -p ${TODIR}

cd org.osate.ocarina.repository/target
cp --recursive repository/* ${TODIR}/.

FROMDIR=org.osate.asap.updatesite/target/repository
TODIR=/var/www/html/download/osate/experimental/osate2-asap
rm -fr ${TODIR}
mkdir -p ${TODIR}
cp --recursive ${FROMDIR}/* ${TODIR}/.
