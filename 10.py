import sqlite3
import os
from DataBase import DataBase

from flask import Flask, render_template, request, flash, session, redirect, url_for, abort

DATADASE = '/tmp/flsk.db'
DEBUG = True
SECRET_KEY = "ASDFGH2345432CVBNM8765FDS9876545678LKJHGFRFCVFR54"

app = Flask(__name__)
app.config.from_object(__name__)

app.config.update({"DATABASE": os.path.join(app.root_path, 'flsk.db')})


def is_authenticated():
    return 'userLogged' in session

def connect_db():
    con = sqlite3.connect(app.config["DATABASE"])
    con.row_factory = sqlite3.Row
    return con



def create_DB():
    db = connect_db()
    with open('sq_db.sql', "r") as f:
        db.cursor().executescript(f.read())
    db.commit()
    db.close()


MICROFLORA_DATABASE = '/tmp/microflora.db'
app.config.update({"MICROFLORA_DATABASE": os.path.join(app.root_path, 'microflora.db')})


def connect_microflora_db():
    con = sqlite3.connect(app.config["MICROFLORA_DATABASE"])
    con.row_factory = sqlite3.Row
    return con


def create_microflora_db():
    db = connect_microflora_db()
    with open('microflora_db.sql', "r") as f:
        db.cursor().executescript(f.read())
    db.commit()
    db.close()


class MicrofloraDB:
    def __init__(self, db):
        self.__db = db
        self.__cur = db.cursor()

    def add_microflora_data(self, microflora_type, count, temperature, humidity, illumination):
        try:
            self.__cur.execute("""
                INSERT INTO microflora (type, count, temperature, humidity, illumination)
                VALUES (?, ?, ?, ?, ?)
            """, (microflora_type, count, temperature, humidity, illumination))
            self.__db.commit()
        except sqlite3.Error as e:
            print("Add microflora data error:", e)
            return False
        return True

    def get_microflora_data(self):
        try:
            self.__cur.execute("SELECT * FROM microflora")
            res = self.__cur.fetchall()
            return res
        except sqlite3.Error as e:
            print("Get microflora data error:", e)
            return []

# menu = [
#         {"name": "Main", "url": "/"},
#         {"name": "Site description", "url": "about"},
#         {"name": "Revers connection", "url": "contacts"},
#
# ]


@app.route("/")
@app.route("/index")
def index():
    db_con = connect_db()
    db = DataBase(db_con)
    return render_template("index.html", title="main",
                           menu=db.get_menu(),
                           posts=db.get_posts())


@app.route('/add_post',methods=["GET", "POST"])
def add_post():
    db_con =connect_db()
    db = DataBase(db_con)

    if request.method == "POST":
        if len(request.form["title"]) > 4 and len(request.form["text"]) > 20:
            res = db.add_post(request.form["title"], request.form["text"], request.form["url"])
            if res:
                flash("Article add successful", category="success")
            else:
                flash("Article add error!", category="error")
        else:
            flash("Article add error!", category="error")

    return render_template("add_post.html",title="Add a new article", menu=db.get_menu())


@app.route('/post/<post_id>')
def show_post(post_id):
    db_con = connect_db()
    db = DataBase(db_con)

    title, post = db.get_post(post_id)
    if not title:
        abort(404)

    return render_template("post.html", title=title, post=post, menu=db.get_menu())



# @app.route("/about")
# def about():
#     return render_template("about.html", menu=menu)


@app.route("/contacts", methods=["GET", "POST"])
def contacts():
    db_con = connect_db()
    db = DataBase(db_con)

    context = {}
    if request.method == "POST":
        if len(request.form["username"]) > 1:
            flash("Message send successful!", category="success")
        else:
            flash("Message send error",category="error")
        context = {
           'username': request.form['username'],
           'email': request.form['email'],
           'message': request.form['message'],
       }

    return render_template("contacts.html", **context, title="Revers connection", menu=db.get_menu())


@app.route('/profile/<username>')
def profile(username):
    if 'userLogged' not in session or session['userLogged'] != username:
        abort(401)
    return f"User {username}"


@app.route('/login', methods = ['GET','POST'])
def login():
    if 'userLogged' in session:
        return redirect(url_for('profile', username = session['userLogged']))
    elif request.method == 'POST' and request.form['username'] == 'admin'\
                                  and request.form['password'] == 'admin':
        session['userLogged'] = request.form['username']
        return redirect(url_for('profile', username = session['userLogged']))
    return render_template('login.html', title="Authorisation", menu = menu)


@app.errorhandler(404)
def page_not_f(error):
    return render_template('page404.html', title='Page is not found', menu=menu, error=error), 404


MICROFLORA_MENU_ITEM = {"name": "Add Microflora Data", "url": "microflora"}


# @app.route("/add_microflora", methods=["GET", "POST"])
# def add_microflora():
#     db_con = connect_microflora_db()
#     microflora_db = MicrofloraDB(db_con)
#
#     if request.method == "POST":
#         microflora_type = request.form.get("microflora_type")
#         count = int(request.form.get("count"))
#         temperature = float(request.form.get("temperature"))
#         humidity = float(request.form.get("humidity"))
#         illumination = float(request.form.get("illumination"))
#
#         res = microflora_db.add_microflora_data(microflora_type, count, temperature, humidity, illumination)
#         if res:
#             flash("Microflora data added successfully!", category="success")
#         else:
#             flash("Error adding microflora data!", category="error")
#
#     return render_template("microflora.html", title="Add Microflora Data", menu=menu)
#
#
# @app.route("/microflora_data")
# def microflora_data():
#     if not is_authenticated():
#         return redirect(url_for('login'))
#
#     db_con = connect_microflora_db()
#     microflora_db = MicrofloraDB(db_con)
#     microflora_data = microflora_db.get_microflora_data()
#
#     return render_template("microflora_data.html", title="Microflora Data", menu=menu, microflora_data=microflora_data)


@app.route("/add_microflora", methods=["GET", "POST"])
def add_microflora():
    if is_authenticated():
        db_con = connect_microflora_db()
        microflora_db = MicrofloraDB(db_con)

        if request.method == "POST":
            microflora_type = request.form.get("microflora_type")
            count = int(request.form.get("count"))
            temperature = float(request.form.get("temperature"))
            humidity = float(request.form.get("humidity"))
            illumination = float(request.form.get("illumination"))

            res = microflora_db.add_microflora_data(microflora_type, count, temperature, humidity, illumination)
            if res:
                flash("Microflora data added successfully!", category="success")
            else:
                flash("Error adding microflora data!", category="error")

        menu = [
            {"name": "Main", "url": "/"},
            {"name": "Site description", "url": "about"},
            {"name": "Revers connection", "url": "contacts"},
            {"name": "Add Microflora Data", "url": "add_microflora"},  # Включаем элемент в меню
            {"name": "Microflora Data", "url": "microflora_data"},      # Включаем элемент в меню
        ]
        return render_template("microflora.html", title="Add Microflora Data", menu=menu)
    else:
        return redirect(url_for('login'))

@app.route("/microflora_data")
def microflora_data():
    if is_authenticated():
        db_con = connect_microflora_db()
        microflora_db = MicrofloraDB(db_con)
        microflora_data = microflora_db.get_microflora_data()

        menu = [
            {"name": "Main", "url": "/"},
            {"name": "Site description", "url": "about"},
            {"name": "Revers connection", "url": "contacts"},
            {"name": "Add Microflora Data", "url": "add_microflora"},  # Включаем элемент в меню
            {"name": "Microflora Data", "url": "microflora_data"},      # Включаем элемент в меню
        ]
        return render_template("microflora_data.html", title="Microflora Data", menu=menu, microflora_data=microflora_data)
    else:
        return redirect(url_for('login'))



if __name__ == "__main__":
    create_DB()
    create_microflora_db()
    menu = [
        {"name": "Main", "url": "/"},
        {"name": "Site description", "url": "about"},
        {"name": "Revers connection", "url": "contacts"},
    ]
    app.run()
