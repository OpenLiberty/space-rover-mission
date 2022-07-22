FROM grafana/grafana:7.3.7

COPY --chown=1000:0 ./datasources.yml /etc/grafana/provisioning/datasources/datasources.yml
COPY --chown=1000:0 ./dashboards.yml /etc/grafana/provisioning/dashboards/dashboards.yml
COPY --chown=1000:0 ./dashboards/open-liberty_rev5.json /var/lib/grafana/dashboards/open-liberty_rev5.json
COPY --chown=1000:0 ./dashboards/space-rover-rev1.json /var/lib/grafana/dashboards/space-rover-rev1.json
