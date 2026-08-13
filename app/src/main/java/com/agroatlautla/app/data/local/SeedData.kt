package com.agroatlautla.app.data.local

object SeedData {
    suspend fun ensureInitialData(database: AgroDatabase) {
        listOf(
            CropEntity(
                name = "Maiz",
                stage = "En crecimiento",
                sowDate = "15 Mar 2026",
                surfaceArea = "2.5 ha",
                irrigationType = "Temporal (lluvia)",
                notes = "Terreno preparado y condiciones del suelo favorables. Revisar humedad y actividad de plagas durante la semana.",
                nextActivity = "Riego - 28 Jun",
                icon = "corn",
                needsSync = false
            ),
            CropEntity(
                name = "Frijol",
                stage = "Floracion",
                sowDate = "20 Mar 2026",
                surfaceArea = "1.2 ha",
                irrigationType = "Goteo",
                notes = "Vigilar aparicion de pulgon en brotes y mantener riego constante.",
                nextActivity = "Fertilizacion - 1 Jul",
                icon = "bean",
                needsSync = false
            ),
            CropEntity(
                name = "Avena",
                stage = "Germinacion",
                sowDate = "10 Abr 2026",
                surfaceArea = "0.8 ha",
                irrigationType = "Temporal (lluvia)",
                notes = "Revisar presencia de chahuixtle en hojas y espigas.",
                nextActivity = "Control de plagas - 5 Jul",
                icon = "wheat",
                needsSync = false
            ),
            CropEntity(
                name = "Hortalizas",
                stage = "Semillero",
                sowDate = "5 May 2026",
                surfaceArea = "0.3 ha",
                irrigationType = "Aspersion",
                notes = "Semillero en invernadero, preparar camas de trasplante.",
                nextActivity = "Trasplante - 10 Jul",
                icon = "leaf",
                needsSync = false
            )
        ).forEach { crop -> upsertCrop(database, crop) }

        listOf(
            CalendarActivityEntity(
                day = 28,
                month = "JUN",
                type = "Riego",
                title = "Riego de maiz",
                cropName = "Maiz",
                colorTag = "blue",
                needsSync = false
            ),
            CalendarActivityEntity(
                day = 1,
                month = "JUL",
                type = "Fertilizacion",
                title = "Fertilizacion frijol",
                cropName = "Frijol",
                colorTag = "orange",
                needsSync = false
            ),
            CalendarActivityEntity(
                day = 5,
                month = "JUL",
                type = "Siembra",
                title = "Control preventivo",
                cropName = "Avena",
                colorTag = "green",
                needsSync = false
            ),
            CalendarActivityEntity(
                day = 10,
                month = "JUL",
                type = "Siembra",
                title = "Trasplante hortalizas",
                cropName = "Hortalizas",
                colorTag = "green",
                needsSync = false
            ),
            CalendarActivityEntity(
                day = 15,
                month = "JUL",
                type = "Riego",
                title = "Riego general",
                cropName = "Todos",
                colorTag = "blue",
                needsSync = false
            ),
            CalendarActivityEntity(
                day = 20,
                month = "JUL",
                type = "Cosecha",
                title = "Cosecha parcial",
                cropName = "Hortalizas",
                colorTag = "orange",
                needsSync = false
            )
        ).forEach { activity -> upsertActivity(database, activity) }

        listOf(
            PestEntity(
                name = "Gusano cogollero",
                affectedCrop = "Maiz",
                severity = "Alta",
                description = "Hojas con agujeros irregulares y excremento oscuro en el cogollo."
            ),
            PestEntity(
                name = "Pulgon negro",
                affectedCrop = "Frijol",
                severity = "Media",
                description = "Colonias negras en brotes, hojas enrolladas y secrecion pegajosa."
            ),
            PestEntity(
                name = "Roya del frijol",
                affectedCrop = "Frijol",
                severity = "Media",
                description = "Pustulas anaranjadas o rojizas en el enves de las hojas."
            ),
            PestEntity(
                name = "Trips del aguacate",
                affectedCrop = "Hortalizas",
                severity = "Alta",
                description = "Plateado en hojas, flores distorsionadas y frutos con cicatrices."
            ),
            PestEntity(
                name = "Chahuixtle",
                affectedCrop = "Avena",
                severity = "Baja",
                description = "Rayas amarillas o estrias cloroticas en hojas y deformacion de espigas."
            ),
            PestEntity(
                name = "Arana roja",
                affectedCrop = "Hortalizas",
                severity = "Alta",
                description = "Puntos amarillos en hojas, telarana fina y hojas bronceadas o secas."
            )
        ).forEach { pest -> upsertPest(database, pest) }

        listOf(
            ExpenseEntity(concept = "Semilla de maiz H-520", amount = 850, date = "10 Mar 2026", category = "Semillas", needsSync = false),
            ExpenseEntity(concept = "Fertilizante DAP 18-46", amount = 1200, date = "12 Mar 2026", category = "Fertilizante", needsSync = false),
            ExpenseEntity(concept = "Flete al campo", amount = 300, date = "15 Mar 2026", category = "Transporte", needsSync = false),
            ExpenseEntity(concept = "Jornales de preparacion", amount = 600, date = "18 Mar 2026", category = "Mano de obra", needsSync = false),
            ExpenseEntity(concept = "Azadones y cubetas", amount = 350, date = "20 Mar 2026", category = "Herramientas", needsSync = false),
            ExpenseEntity(concept = "Semilla de frijol", amount = 150, date = "18 Mar 2026", category = "Semillas", needsSync = false)
        ).forEach { expense -> upsertExpense(database, expense) }
    }

    private suspend fun upsertCrop(database: AgroDatabase, crop: CropEntity) {
        val current = database.cropDao().getByName(crop.name)
        if (current == null) {
            database.cropDao().insert(crop)
        } else if (!current.needsSync) {
            database.cropDao().update(
                current.copy(
                    stage = crop.stage,
                    sowDate = crop.sowDate,
                    surfaceArea = crop.surfaceArea,
                    irrigationType = crop.irrigationType,
                    notes = crop.notes,
                    nextActivity = crop.nextActivity,
                    icon = crop.icon,
                    needsSync = false
                )
            )
        }
    }

    private suspend fun upsertActivity(database: AgroDatabase, activity: CalendarActivityEntity) {
        val current = database.calendarActivityDao().getByTitle(activity.title)
        if (current == null) {
            database.calendarActivityDao().insert(activity)
        } else if (!current.needsSync) {
            database.calendarActivityDao().update(
                current.copy(
                    day = activity.day,
                    month = activity.month,
                    type = activity.type,
                    cropName = activity.cropName,
                    colorTag = activity.colorTag,
                    needsSync = false
                )
            )
        }
    }

    private suspend fun upsertPest(database: AgroDatabase, pest: PestEntity) {
        val current = database.pestDao().getByName(pest.name)
        if (current == null) {
            database.pestDao().insert(pest)
        } else {
            database.pestDao().update(
                current.copy(
                    affectedCrop = pest.affectedCrop,
                    severity = pest.severity,
                    description = pest.description
                )
            )
        }
    }

    private suspend fun upsertExpense(database: AgroDatabase, expense: ExpenseEntity) {
        val current = database.expenseDao().getByConcept(expense.concept)
        if (current == null) {
            database.expenseDao().insert(expense)
        } else if (!current.needsSync) {
            database.expenseDao().update(
                current.copy(
                    amount = expense.amount,
                    date = expense.date,
                    category = expense.category,
                    needsSync = false
                )
            )
        }
    }
}
