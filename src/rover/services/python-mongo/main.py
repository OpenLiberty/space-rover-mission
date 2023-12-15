from pymongo import MongoClient

def get_database():
    CONNECTION_STRING="mongodb://spaceUser:openliberty@mongo:27017/spaceDB"

    client = MongoClient(CONNECTION_STRING)
    return client['spaceDB']

if __name__ == "__main__":
    try: 
        print("getting db")
        dbname = get_database()
        print("done:")
        print(dbname)
    except:
        print("errorr")
    print("end")