FROM registry.access.redhat.com/ubi9/python-311

RUN python -m pip install pymongo

COPY main.py .

CMD ["python", "main.py"]