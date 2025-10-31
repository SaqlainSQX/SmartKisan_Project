from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# !!! REPLACE WITH YOUR POSTGRESQL DETAILS !!!
# "postgresql://<user>:<password>@<host>:<port>/<dbname>"
DATABASE_URL = "postgresql://postgres:Saqlain1675@localhost/myapp_db"

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# Dependency to get DB session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
