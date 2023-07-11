FROM gcr.io/google.com/cloudsdktool/cloud-sdk:emulators

ENV PROJECT_ID=test-project
ENV INSTANCE_ID=test-instance
ENV DATABASE_ID=test-database

RUN export DEBIAN_FRONTEND=noninteractive; \
	apt-get update -y && \
	TZ="Europe/Berlin" apt-get install --no-install-recommends -y bash netcat-openbsd tzdata && \
	apt-get clean && \
	apt-get autoremove -y && \
	rm -rf /var/lib/apt/lists/*

COPY ./scripts/* /usr/local/bin/

HEALTHCHECK  --interval=3s --timeout=3s --retries=15 \
    CMD [ "bash", "-c", "/usr/local/bin/spanner.sh healthcheck || exit 1" ]

EXPOSE 9010
EXPOSE 9020

CMD [ "/usr/local/bin/spanner.sh", "run" ]
