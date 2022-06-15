echo "This script will install the SpacePong application on a clean Ubuntu 20.04 server"

if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

# install required packages (java, authbind, postgres):
apt install -y openjdk-17-jdk openjdk-17-jre
apt install -y authbind
apt install -y postgresql postgresql-contrib

# create a user and group "spacepong" with shell: bash and its own home directory
useradd -d --shell=/bin/bash spacepong

# enable postgresql
systemctl start postgresql.service

# in postgres: create the pongapp user, database, and grant permissions:
psql -U postgres -h << EOF
createuser spacepong;
createdb spacepong;
GRANT ALL ON SCHEMA spacepong to spacepong;
EOF

# copy the contents of this directory into the new directory /home/spacepong/SpacePong
cp ../SpacePong /home/spacepong/SpacePong

# change ownership of new copy to the new user/group "spacepong"
chown --recursive spacepong:spacepong /home/spacepong
chmod --recursive 500 /home/spacepong/SpacePong

# make authbind by port 80 and 443 executable by the user spacepong
touch /etc/authbind/byport/80
touch /etc/authbind/byport/443
chown spacepong:spacepong /etc/authbind/byport/80
chown spacepong:spacepong /etc/authbind/byport/443
chmod 500 /etc/authbind/byport/80
chmod 500 /etc/authbind/byport/443

# for the execution below, be sure to export spring_profiles_active=production
# write the following systemd unit service file:
printf "[Unit]
Description=The SpacePong App
After=syslog.target
Requires=postgresql

[Service]
User=spacepong
ExecStart=authbind java -jar /home/spacepong/SpacePong/target/SpacePong-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target" > /usr/lib/systemd/user/pongapp.service
cd /home/pongapp/PongApp || {
  echo "could not switch to pongapp, exiting"
  exit
}
sudo -u pongapp ./mvnw package
systemctl enable pongapp.service
systemctl start pongapp.service
