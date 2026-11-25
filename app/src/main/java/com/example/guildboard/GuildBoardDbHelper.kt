package com.example.guildboard

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GuildBoardDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "guildboard_local.db"
        private const val DATABASE_VERSION = 4

        // Tabela Użytkowników
        private const val TABLE_USERS = "users"
        private const val KEY_USER_ID = "id"
        private const val KEY_USER_NICK = "nickname"
        private const val KEY_USER_EMAIL = "email"
        private const val KEY_USER_PASS = "password"
        private const val KEY_USER_ROLE = "role_and_level"
        private const val KEY_USER_CITY = "city"
        private const val KEY_USER_DISTANCE = "distance_text"
        private const val KEY_USER_GAMES = "owned_games"
        private const val KEY_USER_GAMES_PLAYED = "games_played"
        private const val KEY_USER_TABLES_ORG = "tables_organized"
        private const val KEY_USER_RATING = "rating"

        // Tabela Wydarzeń / Stołów (powiązana z users za pomocą organizer_id)
        private const val TABLE_EVENTS = "game_events"
        private const val KEY_EVENT_ID = "id"
        private const val KEY_EVENT_TITLE = "title"
        private const val KEY_EVENT_DESC = "description"
        private const val KEY_EVENT_DIST = "distance_km"
        private const val KEY_EVENT_CURR_SEATS = "current_seats"
        private const val KEY_EVENT_MAX_SEATS = "max_seats"
        private const val KEY_EVENT_CATEGORY = "category"
        private const val KEY_EVENT_ORGANIZER_ID = "organizer_id"
        private const val KEY_EVENT_ORGANIZER_NICK = "organizer_nick"
        private const val KEY_EVENT_JOINED = "is_joined"

        // Tabela Wiadomości Czatu Stołu
        private const val TABLE_MESSAGES = "chat_messages"
        private const val KEY_MSG_ID = "id"
        private const val KEY_MSG_EVENT_ID = "event_id"
        private const val KEY_MSG_SENDER = "sender_name"
        private const val KEY_MSG_TEXT = "message_text"
        private const val KEY_MSG_TIME = "timestamp"
        private const val KEY_MSG_SYSTEM = "is_system"

        // Tabela Gildii / Grup
        private const val TABLE_GUILDS = "guilds"
        private const val KEY_GUILD_ID = "id"
        private const val KEY_GUILD_NAME = "name"
        private const val KEY_GUILD_DESC = "description"
        private const val KEY_GUILD_OWNER_ID = "owner_id"
        private const val KEY_GUILD_OWNER_NICK = "owner_nick"
        private const val KEY_GUILD_CITY = "city"
        private const val KEY_GUILD_REQUIRES_APPROVAL = "requires_approval"
        private const val KEY_GUILD_MEMBER_COUNT = "member_count"

        // Tabela Członków Gildii
        private const val TABLE_GUILD_MEMBERS = "guild_members"
        private const val KEY_GM_ID = "id"
        private const val KEY_GM_GUILD_ID = "guild_id"
        private const val KEY_GM_USER_ID = "user_id"
        private const val KEY_GM_USER_NICK = "user_nick"
        private const val KEY_GM_ROLE = "role" // OWNER, MODERATOR, MEMBER
        private const val KEY_GM_STATUS = "status" // ACCEPTED, PENDING

        // Tabela Czatów Gildii
        private const val TABLE_GUILD_CHAT = "guild_chat_messages"
        private const val KEY_GC_ID = "id"
        private const val KEY_GC_GUILD_ID = "guild_id"
        private const val KEY_GC_SENDER = "sender_name"
        private const val KEY_GC_TEXT = "message_text"
        private const val KEY_GC_TIME = "timestamp"
        private const val KEY_GC_SYSTEM = "is_system"

        // Tabela Gier Gildii
        private const val TABLE_GUILD_GAMES = "guild_games"
        private const val KEY_GG_ID = "id"
        private const val KEY_GG_GUILD_ID = "guild_id"
        private const val KEY_GG_TITLE = "game_title"
        private const val KEY_GG_CATEGORY = "category"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $KEY_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_USER_NICK TEXT UNIQUE NOT NULL,
                $KEY_USER_EMAIL TEXT UNIQUE NOT NULL,
                $KEY_USER_PASS TEXT NOT NULL,
                $KEY_USER_ROLE TEXT,
                $KEY_USER_CITY TEXT,
                $KEY_USER_DISTANCE TEXT,
                $KEY_USER_GAMES TEXT,
                $KEY_USER_GAMES_PLAYED INTEGER DEFAULT 0,
                $KEY_USER_TABLES_ORG INTEGER DEFAULT 0,
                $KEY_USER_RATING REAL DEFAULT 0.0
            )
        """.trimIndent()

        val createEventsTable = """
            CREATE TABLE $TABLE_EVENTS (
                $KEY_EVENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_EVENT_TITLE TEXT NOT NULL,
                $KEY_EVENT_DESC TEXT,
                $KEY_EVENT_DIST REAL,
                $KEY_EVENT_CURR_SEATS INTEGER,
                $KEY_EVENT_MAX_SEATS INTEGER,
                $KEY_EVENT_CATEGORY TEXT,
                $KEY_EVENT_ORGANIZER_ID INTEGER,
                $KEY_EVENT_ORGANIZER_NICK TEXT,
                $KEY_EVENT_JOINED INTEGER DEFAULT 0,
                FOREIGN KEY ($KEY_EVENT_ORGANIZER_ID) REFERENCES $TABLE_USERS($KEY_USER_ID)
            )
        """.trimIndent()

        val createMessagesTable = """
            CREATE TABLE $TABLE_MESSAGES (
                $KEY_MSG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_MSG_EVENT_ID INTEGER,
                $KEY_MSG_SENDER TEXT,
                $KEY_MSG_TEXT TEXT,
                $KEY_MSG_TIME TEXT,
                $KEY_MSG_SYSTEM INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createGuildsTable = """
            CREATE TABLE $TABLE_GUILDS (
                $KEY_GUILD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_GUILD_NAME TEXT NOT NULL,
                $KEY_GUILD_DESC TEXT,
                $KEY_GUILD_OWNER_ID INTEGER,
                $KEY_GUILD_OWNER_NICK TEXT,
                $KEY_GUILD_CITY TEXT,
                $KEY_GUILD_REQUIRES_APPROVAL INTEGER DEFAULT 0,
                $KEY_GUILD_MEMBER_COUNT INTEGER DEFAULT 1,
                FOREIGN KEY ($KEY_GUILD_OWNER_ID) REFERENCES $TABLE_USERS($KEY_USER_ID)
            )
        """.trimIndent()

        val createGuildMembersTable = """
            CREATE TABLE $TABLE_GUILD_MEMBERS (
                $KEY_GM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_GM_GUILD_ID INTEGER,
                $KEY_GM_USER_ID INTEGER,
                $KEY_GM_USER_NICK TEXT,
                $KEY_GM_ROLE TEXT,
                $KEY_GM_STATUS TEXT,
                FOREIGN KEY ($KEY_GM_GUILD_ID) REFERENCES $TABLE_GUILDS($KEY_GUILD_ID),
                FOREIGN KEY ($KEY_GM_USER_ID) REFERENCES $TABLE_USERS($KEY_USER_ID)
            )
        """.trimIndent()

        val createGuildChatTable = """
            CREATE TABLE $TABLE_GUILD_CHAT (
                $KEY_GC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_GC_GUILD_ID INTEGER,
                $KEY_GC_SENDER TEXT,
                $KEY_GC_TEXT TEXT,
                $KEY_GC_TIME TEXT,
                $KEY_GC_SYSTEM INTEGER DEFAULT 0,
                FOREIGN KEY ($KEY_GC_GUILD_ID) REFERENCES $TABLE_GUILDS($KEY_GUILD_ID)
            )
        """.trimIndent()

        val createGuildGamesTable = """
            CREATE TABLE $TABLE_GUILD_GAMES (
                $KEY_GG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_GG_GUILD_ID INTEGER,
                $KEY_GG_TITLE TEXT NOT NULL,
                $KEY_GG_CATEGORY TEXT,
                FOREIGN KEY ($KEY_GG_GUILD_ID) REFERENCES $TABLE_GUILDS($KEY_GUILD_ID)
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createEventsTable)
        db.execSQL(createMessagesTable)
        db.execSQL(createGuildsTable)
        db.execSQL(createGuildMembersTable)
        db.execSQL(createGuildChatTable)
        db.execSQL(createGuildGamesTable)

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GUILDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GUILD_MEMBERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GUILD_CHAT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GUILD_GAMES")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        db.execSQL("INSERT INTO $TABLE_USERS ($KEY_USER_NICK, $KEY_USER_EMAIL, $KEY_USER_PASS, $KEY_USER_ROLE, $KEY_USER_CITY, $KEY_USER_DISTANCE, $KEY_USER_GAMES, $KEY_USER_GAMES_PLAYED, $KEY_USER_TABLES_ORG, $KEY_USER_RATING) VALUES ('Jan_Gracz', 'jan@example.com', 'Password123', 'Organizator • Zaawansowany', 'Kraków', 'Kraków (1.0 km)', 'Nemesis, Catan, Terraforming Mars', 14, 5, 4.9)")
        db.execSQL("INSERT INTO $TABLE_USERS ($KEY_USER_NICK, $KEY_USER_EMAIL, $KEY_USER_PASS, $KEY_USER_ROLE, $KEY_USER_CITY, $KEY_USER_DISTANCE, $KEY_USER_GAMES, $KEY_USER_GAMES_PLAYED, $KEY_USER_TABLES_ORG, $KEY_USER_RATING) VALUES ('Piotr_RPG', 'piotr@example.com', 'Password123', 'Mistrz Gry • Zaawansowany', 'Kraków', 'Kraków (2.5 km)', 'Nemesis, Gloomhaven, Brass Birmingham', 28, 12, 5.0)")
        db.execSQL("INSERT INTO $TABLE_USERS ($KEY_USER_NICK, $KEY_USER_EMAIL, $KEY_USER_PASS, $KEY_USER_ROLE, $KEY_USER_CITY, $KEY_USER_DISTANCE, $KEY_USER_GAMES, $KEY_USER_GAMES_PLAYED, $KEY_USER_TABLES_ORG, $KEY_USER_RATING) VALUES ('Ania_Boardgames', 'ania@example.com', 'Password123', 'Gracz • Średniozaawansowany', 'Kraków', 'Kraków (1.2 km)', 'Catan, Everdell, Wingspan', 8, 1, 4.8)")

        // Powiązanie stołów z użytkownikami (Jan_Gracz - 1, Piotr_RPG - 2, Ania_Boardgames - 3)
        db.execSQL("INSERT INTO $TABLE_EVENTS ($KEY_EVENT_TITLE, $KEY_EVENT_DESC, $KEY_EVENT_DIST, $KEY_EVENT_CURR_SEATS, $KEY_EVENT_MAX_SEATS, $KEY_EVENT_CATEGORY, $KEY_EVENT_ORGANIZER_ID, $KEY_EVENT_ORGANIZER_NICK) VALUES ('Nemesis Lockdown', 'Surwiwal horror z figurkami. Szukamy 2 doświadczonych graczy.', 3.2, 3, 5, 'Planszówki', 1, 'Jan_Gracz')")
        db.execSQL("INSERT INTO $TABLE_EVENTS ($KEY_EVENT_TITLE, $KEY_EVENT_DESC, $KEY_EVENT_DIST, $KEY_EVENT_CURR_SEATS, $KEY_EVENT_MAX_SEATS, $KEY_EVENT_CATEGORY, $KEY_EVENT_ORGANIZER_ID, $KEY_EVENT_ORGANIZER_NICK) VALUES ('Catan: Miasta i Rycerze', 'Szybka rozgrywka dla każdego. Zapewniamy przekąski!', 1.5, 2, 4, 'Planszówki', 3, 'Ania_Boardgames')")
        db.execSQL("INSERT INTO $TABLE_EVENTS ($KEY_EVENT_TITLE, $KEY_EVENT_DESC, $KEY_EVENT_DIST, $KEY_EVENT_CURR_SEATS, $KEY_EVENT_MAX_SEATS, $KEY_EVENT_CATEGORY, $KEY_EVENT_ORGANIZER_ID, $KEY_EVENT_ORGANIZER_NICK) VALUES ('Dungeons & Dragons 5e - Klątwa Strahda', 'Mroczna kampania RPG. Szukamy gracza do stałej grupy.', 5.0, 4, 5, 'Sesje RPG', 2, 'Piotr_RPG')")

        db.execSQL("INSERT INTO $TABLE_MESSAGES ($KEY_MSG_EVENT_ID, $KEY_MSG_SENDER, $KEY_MSG_TEXT, $KEY_MSG_TIME, $KEY_MSG_SYSTEM) VALUES (1, 'System', 'Połączono ze stolikiem: Nemesis Lockdown', '17:00', 1)")

        // Gildie
        db.execSQL("INSERT INTO $TABLE_GUILDS ($KEY_GUILD_NAME, $KEY_GUILD_DESC, $KEY_GUILD_OWNER_ID, $KEY_GUILD_OWNER_NICK, $KEY_GUILD_CITY, $KEY_GUILD_REQUIRES_APPROVAL, $KEY_GUILD_MEMBER_COUNT) VALUES ('Krakowska Gildia Planszówkowa', 'Oficjalna grupa graczy z Krakowa. Spotkania w każdy piątek.', 1, 'Jan_Gracz', 'Kraków', 0, 2)")
        db.execSQL("INSERT INTO $TABLE_GUILDS ($KEY_GUILD_NAME, $KEY_GUILD_DESC, $KEY_GUILD_OWNER_ID, $KEY_GUILD_OWNER_NICK, $KEY_GUILD_CITY, $KEY_GUILD_REQUIRES_APPROVAL, $KEY_GUILD_MEMBER_COUNT) VALUES ('RPG Małopolska - Mistrzowie Mroku', 'Zamknięta grupa dla graczy i Mistrzów Gry systemów D&D, CoC oraz Neuroshima.', 2, 'Piotr_RPG', 'Kraków', 1, 1)")

        // Członkowie Gildii 1
        db.execSQL("INSERT INTO $TABLE_GUILD_MEMBERS ($KEY_GM_GUILD_ID, $KEY_GM_USER_ID, $KEY_GM_USER_NICK, $KEY_GM_ROLE, $KEY_GM_STATUS) VALUES (1, 1, 'Jan_Gracz', 'OWNER', 'ACCEPTED')")
        db.execSQL("INSERT INTO $TABLE_GUILD_MEMBERS ($KEY_GM_GUILD_ID, $KEY_GM_USER_ID, $KEY_GM_USER_NICK, $KEY_GM_ROLE, $KEY_GM_STATUS) VALUES (1, 3, 'Ania_Boardgames', 'MODERATOR', 'ACCEPTED')")

        // Członkowie Gildii 2
        db.execSQL("INSERT INTO $TABLE_GUILD_MEMBERS ($KEY_GM_GUILD_ID, $KEY_GM_USER_ID, $KEY_GM_USER_NICK, $KEY_GM_ROLE, $KEY_GM_STATUS) VALUES (2, 2, 'Piotr_RPG', 'OWNER', 'ACCEPTED')")

        // Czat Gildii 1
        db.execSQL("INSERT INTO $TABLE_GUILD_CHAT ($KEY_GC_GUILD_ID, $KEY_GC_SENDER, $KEY_GC_TEXT, $KEY_GC_TIME, $KEY_GC_SYSTEM) VALUES (1, 'System', 'Witaj na czacie Krakowskiej Gildii Planszówkowej!', '12:00', 1)")
        db.execSQL("INSERT INTO $TABLE_GUILD_CHAT ($KEY_GC_GUILD_ID, $KEY_GC_SENDER, $KEY_GC_TEXT, $KEY_GC_TIME, $KEY_GC_SYSTEM) VALUES (1, 'Jan_Gracz', 'Cześć wszystkim! Co przynosicie na najbliższe spotkanie?', '12:05', 0)")

        // Gry Gildii 1
        db.execSQL("INSERT INTO $TABLE_GUILD_GAMES ($KEY_GG_GUILD_ID, $KEY_GG_TITLE, $KEY_GG_CATEGORY) VALUES (1, 'Catan: Miasta i Rycerze', 'Planszówki')")
        db.execSQL("INSERT INTO $TABLE_GUILD_GAMES ($KEY_GG_GUILD_ID, $KEY_GG_TITLE, $KEY_GG_CATEGORY) VALUES (1, 'Nemesis Lockdown', 'Planszówki')")
    }

    // --- METODY DLA STOŁÓW / WYDARZEŃ ---
    fun getAllGameEvents(): List<GameEvent> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_EVENTS", null)
        val list = mutableListOf<GameEvent>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_EVENT_ID)).toString()
            val title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EVENT_TITLE))
            val desc = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EVENT_DESC))
            val dist = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_EVENT_DIST))
            val currSeats = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_EVENT_CURR_SEATS))
            val maxSeats = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_EVENT_MAX_SEATS))
            val category = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EVENT_CATEGORY))
            val orgId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_EVENT_ORGANIZER_ID))
            val orgNick = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EVENT_ORGANIZER_NICK)) ?: "Jan_Gracz"
            val joined = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_EVENT_JOINED)) == 1

            list.add(GameEvent(id, title, desc, dist, currSeats, maxSeats, category, joined, orgId, orgNick))
        }
        cursor.close()
        return list
    }

    fun updateEventJoinedStatus(eventId: String, isJoined: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_EVENT_JOINED, if (isJoined) 1 else 0)
        }
        db.update(TABLE_EVENTS, values, "$KEY_EVENT_ID = ?", arrayOf(eventId))
    }

    // --- SPRAWDZENIE GILDII UŻYTKOWNIKA ---
    fun getUserGuild(userId: Long): Guild? {
        val db = readableDatabase
        val query = """
            SELECT g.* FROM $TABLE_GUILDS g
            INNER JOIN $TABLE_GUILD_MEMBERS gm ON g.$KEY_GUILD_ID = gm.$KEY_GM_GUILD_ID
            WHERE gm.$KEY_GM_USER_ID = ? AND gm.$KEY_GM_STATUS = 'ACCEPTED'
            LIMIT 1
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        var guild: Guild? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_GUILD_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GUILD_NAME))
            val desc = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GUILD_DESC))
            val ownerId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_GUILD_OWNER_ID))
            val ownerNick = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GUILD_OWNER_NICK))
            val city = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GUILD_CITY))
            val reqApp = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GUILD_REQUIRES_APPROVAL)) == 1
            val count = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GUILD_MEMBER_COUNT))

            guild = Guild(id, name, desc, ownerId, ownerNick, city, reqApp, count)
        }
        cursor.close()
        return guild
    }

    fun getUserGuildRole(guildId: Long, userId: Long): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $KEY_GM_ROLE FROM $TABLE_GUILD_MEMBERS WHERE $KEY_GM_GUILD_ID = ? AND $KEY_GM_USER_ID = ? AND $KEY_GM_STATUS = 'ACCEPTED'", arrayOf(guildId.toString(), userId.toString()))
        var role: String? = null
        if (cursor.moveToFirst()) {
            role = cursor.getString(0)
        }
        cursor.close()
        return role
    }

    fun isUserInAnyGuild(userId: Long): Boolean {
        return getUserGuild(userId) != null
    }

    // --- PRZEKAZANIE WŁASNOŚCI GILDII ---
    fun transferGuildOwnership(guildId: Long, currentOwnerUserId: Long, newOwnerMemberId: Long, newOwnerUserId: Long, newOwnerNick: String): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val valuesOldOwner = ContentValues().apply { put(KEY_GM_ROLE, "MEMBER") }
            db.update(TABLE_GUILD_MEMBERS, valuesOldOwner, "$KEY_GM_GUILD_ID = ? AND $KEY_GM_USER_ID = ?", arrayOf(guildId.toString(), currentOwnerUserId.toString()))

            val valuesNewOwner = ContentValues().apply { put(KEY_GM_ROLE, "OWNER") }
            db.update(TABLE_GUILD_MEMBERS, valuesNewOwner, "$KEY_GM_ID = ?", arrayOf(newOwnerMemberId.toString()))

            val valuesGuild = ContentValues().apply {
                put(KEY_GUILD_OWNER_ID, newOwnerUserId)
                put(KEY_GUILD_OWNER_NICK, newOwnerNick)
            }
            db.update(TABLE_GUILDS, valuesGuild, "$KEY_GUILD_ID = ?", arrayOf(guildId.toString()))

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            return false
        } finally {
            db.endTransaction()
        }
    }

    // --- CZAT GILDII ---
    fun getGuildChatMessages(guildId: Long): List<ChatMessage> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_GUILD_CHAT WHERE $KEY_GC_GUILD_ID = ?", arrayOf(guildId.toString()))
        val list = mutableListOf<ChatMessage>()
        while (cursor.moveToNext()) {
            val sender = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GC_SENDER))
            val text = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GC_TEXT))
            val time = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GC_TIME))
            val system = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GC_SYSTEM)) == 1

            list.add(ChatMessage(sender, time, text, system))
        }
        cursor.close()
        return list
    }

    fun addGuildChatMessage(guildId: Long, message: ChatMessage): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_GC_GUILD_ID, guildId)
            put(KEY_GC_SENDER, message.senderName)
            put(KEY_GC_TEXT, message.messageText)
            put(KEY_GC_TIME, message.time)
            put(KEY_GC_SYSTEM, if (message.isSystem) 1 else 0)
        }
        return db.insert(TABLE_GUILD_CHAT, null, values)
    }

    // --- GRY GILDII ---
    fun getGuildGames(guildId: Long): List<GuildGame> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_GUILD_GAMES WHERE $KEY_GG_GUILD_ID = ?", arrayOf(guildId.toString()))
        val list = mutableListOf<GuildGame>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_GG_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GG_TITLE))
            val category = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GG_CATEGORY)) ?: "Planszówki"

            list.add(GuildGame(id, guildId, title, category))
        }
        cursor.close()
        return list
    }

    fun addGuildGame(guildId: Long, title: String, category: String = "Planszówki"): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_GG_GUILD_ID, guildId)
            put(KEY_GG_TITLE, title)
            put(KEY_GG_CATEGORY, category)
        }
        return db.insert(TABLE_GUILD_GAMES, null, values)
    }

    // --- OPUSZCZANIE / USUWANIE GILDII ---
    fun leaveOrDisbandGuild(guildId: Long, userId: Long): Boolean {
        val db = writableDatabase
        val role = getUserGuildRole(guildId, userId)

        if (role == "OWNER") {
            db.delete(TABLE_GUILDS, "$KEY_GUILD_ID = ?", arrayOf(guildId.toString()))
            db.delete(TABLE_GUILD_MEMBERS, "$KEY_GM_GUILD_ID = ?", arrayOf(guildId.toString()))
            db.delete(TABLE_GUILD_CHAT, "$KEY_GC_GUILD_ID = ?", arrayOf(guildId.toString()))
            db.delete(TABLE_GUILD_GAMES, "$KEY_GG_GUILD_ID = ?", arrayOf(guildId.toString()))
            return true
        } else {
            val rows = db.delete(TABLE_GUILD_MEMBERS, "$KEY_GM_GUILD_ID = ? AND $KEY_GM_USER_ID = ?", arrayOf(guildId.toString(), userId.toString()))
            if (rows > 0) {
                db.execSQL("UPDATE $TABLE_GUILDS SET $KEY_GUILD_MEMBER_COUNT = MAX(1, $KEY_GUILD_MEMBER_COUNT - 1) WHERE $KEY_GUILD_ID = $guildId")
                return true
            }
        }
        return false
    }

    // --- WERYFIKACJA I LOGOWANIE ---
    fun isUserExists(nickname: String, email: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $KEY_USER_ID FROM $TABLE_USERS WHERE $KEY_USER_NICK = ? OR $KEY_USER_EMAIL = ?", arrayOf(nickname, email))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun registerUser(user: User): Long {
        if (isUserExists(user.nickname, user.email)) {
            return -1L
        }

        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_USER_NICK, user.nickname)
            put(KEY_USER_EMAIL, user.email)
            put(KEY_USER_PASS, user.password)
            put(KEY_USER_ROLE, user.roleAndLevel)
            put(KEY_USER_CITY, user.city)
            put(KEY_USER_DISTANCE, "${user.city} (0.5 km)")
            put(KEY_USER_GAMES, "")
            put(KEY_USER_GAMES_PLAYED, 0)
            put(KEY_USER_TABLES_ORG, 0)
            put(KEY_USER_RATING, 0.0)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun updateUserProfile(userId: Long, newNick: String, newCity: String, newGames: String, newRole: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_USER_NICK, newNick)
            put(KEY_USER_CITY, newCity)
            put(KEY_USER_DISTANCE, "$newCity (0.5 km)")
            put(KEY_USER_GAMES, newGames)
            put(KEY_USER_ROLE, newRole)
        }
        val rows = db.update(TABLE_USERS, values, "$KEY_USER_ID = ?", arrayOf(userId.toString()))
        return rows > 0
    }

    fun authenticateUser(emailOrNick: String, pass: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE ($KEY_USER_NICK = ? OR $KEY_USER_EMAIL = ?) AND $KEY_USER_PASS = ?",
            arrayOf(emailOrNick, emailOrNick, pass)
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor)
        }
        cursor.close()
        return user
    }

    fun getUserByNick(nick: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $KEY_USER_NICK = ?", arrayOf(nick))
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor)
        }
        cursor.close()
        return user
    }

    private fun cursorToUser(cursor: android.database.Cursor): User {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_USER_ID))
        val nick = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NICK))
        val email = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL))
        val password = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PASS))
        val role = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ROLE)) ?: "Gracz"
        val city = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_CITY)) ?: "Kraków"
        val dist = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_DISTANCE)) ?: "Kraków"
        val games = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_GAMES)) ?: ""
        val gamesPlayed = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_GAMES_PLAYED))
        val tablesOrg = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_TABLES_ORG))
        val rating = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_USER_RATING))

        return User(id, nick, email, password, role, city, dist, games, gamesPlayed, tablesOrg, rating)
    }

    fun getAllPlayers(): List<PlayerProfile> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS", null)
        val list = mutableListOf<PlayerProfile>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_USER_ID)).toString()
            val nick = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NICK))
            val role = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ROLE)) ?: "Gracz"
            val dist = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_DISTANCE)) ?: "Kraków"
            val games = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_GAMES)) ?: ""

            val gamesText = if (games.isNotEmpty()) "Posiadam: $games" else "Brak podanych gier"
            list.add(PlayerProfile(id, nick, role, dist, gamesText))
        }
        cursor.close()
        return list
    }

    fun getAllGuilds(): List<Guild> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_GUILDS", null)
        val list = mutableListOf<Guild>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_GUILD_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GUILD_NAME))
            val desc = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GUILD_DESC))
            val ownerId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_GUILD_OWNER_ID))
            val ownerNick = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GUILD_OWNER_NICK))
            val city = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GUILD_CITY))
            val reqApp = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GUILD_REQUIRES_APPROVAL)) == 1
            val count = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_GUILD_MEMBER_COUNT))

            list.add(Guild(id, name, desc, ownerId, ownerNick, city, reqApp, count))
        }
        cursor.close()
        return list
    }

    fun createGuild(guild: Guild): Long {
        if (isUserInAnyGuild(guild.ownerId)) {
            return -1L
        }

        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_GUILD_NAME, guild.name)
            put(KEY_GUILD_DESC, guild.description)
            put(KEY_GUILD_OWNER_ID, guild.ownerId)
            put(KEY_GUILD_OWNER_NICK, guild.ownerNick)
            put(KEY_GUILD_CITY, guild.city)
            put(KEY_GUILD_REQUIRES_APPROVAL, if (guild.requiresApproval) 1 else 0)
            put(KEY_GUILD_MEMBER_COUNT, 1)
        }
        val guildId = db.insert(TABLE_GUILDS, null, values)

        if (guildId > 0) {
            val memberValues = ContentValues().apply {
                put(KEY_GM_GUILD_ID, guildId)
                put(KEY_GM_USER_ID, guild.ownerId)
                put(KEY_GM_USER_NICK, guild.ownerNick)
                put(KEY_GM_ROLE, "OWNER")
                put(KEY_GM_STATUS, "ACCEPTED")
            }
            db.insert(TABLE_GUILD_MEMBERS, null, memberValues)
        }
        return guildId
    }

    fun joinGuild(guildId: Long, userId: Long, userNick: String, requiresApproval: Boolean): String {
        if (isUserInAnyGuild(userId)) {
            return "ALREADY_IN_GUILD"
        }

        val db = writableDatabase
        val status = if (requiresApproval) "PENDING" else "ACCEPTED"
        val values = ContentValues().apply {
            put(KEY_GM_GUILD_ID, guildId)
            put(KEY_GM_USER_ID, userId)
            put(KEY_GM_USER_NICK, userNick)
            put(KEY_GM_ROLE, "MEMBER")
            put(KEY_GM_STATUS, status)
        }
        db.insert(TABLE_GUILD_MEMBERS, null, values)

        if (!requiresApproval) {
            db.execSQL("UPDATE $TABLE_GUILDS SET $KEY_GUILD_MEMBER_COUNT = $KEY_GUILD_MEMBER_COUNT + 1 WHERE $KEY_GUILD_ID = $guildId")
        }

        return status
    }

    fun getGuildMembers(guildId: Long): List<GuildMember> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_GUILD_MEMBERS WHERE $KEY_GM_GUILD_ID = ?", arrayOf(guildId.toString()))
        val list = mutableListOf<GuildMember>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_GM_ID))
            val uId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_GM_USER_ID))
            val nick = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GM_USER_NICK))
            val role = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GM_ROLE))
            val status = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GM_STATUS))

            list.add(GuildMember(id, guildId, uId, nick, role, status))
        }
        cursor.close()
        return list
    }

    fun updateGuildMemberRole(memberId: Long, newRole: String) {
        val db = writableDatabase
        val values = ContentValues().apply { put(KEY_GM_ROLE, newRole) }
        db.update(TABLE_GUILD_MEMBERS, values, "$KEY_GM_ID = ?", arrayOf(memberId.toString()))
    }

    fun updateGuildMemberStatus(guildId: Long, memberId: Long, newStatus: String) {
        val db = writableDatabase
        val values = ContentValues().apply { put(KEY_GM_STATUS, newStatus) }
        db.update(TABLE_GUILD_MEMBERS, values, "$KEY_GM_ID = ?", arrayOf(memberId.toString()))
        if (newStatus == "ACCEPTED") {
            db.execSQL("UPDATE $TABLE_GUILDS SET $KEY_GUILD_MEMBER_COUNT = $KEY_GUILD_MEMBER_COUNT + 1 WHERE $KEY_GUILD_ID = $guildId")
        }
    }

    fun removeGuildMember(guildId: Long, memberId: Long) {
        val db = writableDatabase
        db.delete(TABLE_GUILD_MEMBERS, "$KEY_GM_ID = ?", arrayOf(memberId.toString()))
        db.execSQL("UPDATE $TABLE_GUILDS SET $KEY_GUILD_MEMBER_COUNT = MAX(1, $KEY_GUILD_MEMBER_COUNT - 1) WHERE $KEY_GUILD_ID = $guildId")
    }

    fun getChatMessages(eventId: Int = 1): List<ChatMessage> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MESSAGES WHERE $KEY_MSG_EVENT_ID = ?", arrayOf(eventId.toString()))
        val list = mutableListOf<ChatMessage>()
        while (cursor.moveToNext()) {
            val sender = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MSG_SENDER))
            val text = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MSG_TEXT))
            val time = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MSG_TIME))
            val system = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MSG_SYSTEM)) == 1

            list.add(ChatMessage(sender, time, text, system))
        }
        cursor.close()
        return list
    }

    fun addChatMessage(eventId: Int = 1, message: ChatMessage): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_MSG_EVENT_ID, eventId)
            put(KEY_MSG_SENDER, message.senderName)
            put(KEY_MSG_TEXT, message.messageText)
            put(KEY_MSG_TIME, message.time)
            put(KEY_MSG_SYSTEM, if (message.isSystem) 1 else 0)
        }
        return db.insert(TABLE_MESSAGES, null, values)
    }
}