SLES-Init-Script fuer Dive4Elements River:

Installation als root:
- d4e-river Kopieren nach /etc/init.d/d4e-river
- chmod 755 /etc/init.d/d4e-river
- insserv /etc/init.d/d4e-river
- d4e-start Kopieren nach /opt/flys/d4e-start
- chmod 755 /opt/flys/d4e-start
- /etc/init.d/d4e-river start


Deinstallation als root:
- /etc/init.d/d4e-river stop
- insserv -r /etc/init.d/d4e-river
- rm /var/log/d4e-river.log /var/run/d4e-river.pid /etc/init.d/d4e-river
- rm /opt/flys/d4e-start

TODO:
- ggf. logrotate fuer Logdatei /var/log/d4e-river.log konfigurieren
