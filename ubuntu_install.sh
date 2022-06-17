echo "This script will install the SpacePong application on a clean Ubuntu 20.04 server"

if [ "$EUID" -ne 0 ]; then
  echo "Please run as root"
  exit
fi

# install required packages (java, authbind, postgres):
apt install -y openjdk-17-jdk openjdk-17-jre
apt install -y authbind
apt install -y postgresql postgresql-contrib

# create a user and group "spacepong" with shell: bash and its own home directory
useradd --shell=/bin/bash spacepong

# enable postgresql
systemctl start postgresql.service

# in postgres: create the pongapp user, database, and grant permissions:
psql -U postgres -h << EOF
CREATE USER spacepong;
CREATE DATABASE spacepong;
ALTER DATABASE spacepong OWNER TO spacepong;
EOF

# set up sftp for easy code deployment
mkdir -p /var/sftp/uploads
chown root:root /var/sftp
chmod 755 /var/sftp
chown spacepong:spacepong /var/sftp/uploads
# ...
# TODO: incorporate the rest of the instructions from the following link:
# https://www.digitalocean.com/community/tutorials/how-to-enable-sftp-without-shell-access-on-ubuntu-20-04
# ...


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
ExecStart=env spring_profiles_active=production authbind java -jar /var/sftp/uploads/target/SpacePong-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target" > /etc/systemd/system/pongapp.service
cd /var/sftp/uploads || {
  echo "could not switch to pongapp, exiting"
  exit 1
}
sudo -u spacepong ./mvnw package
systemctl enable pongapp.service
systemctl start pongapp.service
sleep 1
systemctl status pongapp
