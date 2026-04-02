package co.ke.snilloc.uberclone.data.model

data class Country(
    val name: String,
    val code: String,
    val phoneCode: String,
    val flag: String,
    val phoneFormat: String
) {
    companion object {
        fun getEastAfricanCountries(): List<Country> {
            return listOf(
                Country(
                    name = "Kenya",
                    code = "KE",
                    phoneCode = "+254",
                    flag = "🇰🇪",
                    phoneFormat = "0XXX XXX XXX"
                ),
                Country(
                    name = "Uganda",
                    code = "UG",
                    phoneCode = "+256",
                    flag = "🇺🇬",
                    phoneFormat = "0XXX XXX XXX"
                ),
                Country(
                    name = "Tanzania",
                    code = "TZ",
                    phoneCode = "+255",
                    flag = "🇹🇿",
                    phoneFormat = "0XXX XXX XXX"
                ),
                Country(
                    name = "Rwanda",
                    code = "RW",
                    phoneCode = "+250",
                    flag = "🇷🇼",
                    phoneFormat = "0XXX XXX XXX"
                ),
                Country(
                    name = "Burundi",
                    code = "BI",
                    phoneCode = "+257",
                    flag = "🇧🇮",
                    phoneFormat = "XX XXX XXX"
                ),
                Country(
                    name = "Ethiopia",
                    code = "ET",
                    phoneCode = "+251",
                    flag = "🇪🇹",
                    phoneFormat = "0XX XXX XXXX"
                ),
                Country(
                    name = "South Sudan",
                    code = "SS",
                    phoneCode = "+211",
                    flag = "🇸🇸",
                    phoneFormat = "0XX XXX XXX"
                ),
                Country(
                    name = "Djibouti",
                    code = "DJ",
                    phoneCode = "+253",
                    flag = "🇩🇯",
                    phoneFormat = "XX XX XX XX"
                ),
                Country(
                    name = "Eritrea",
                    code = "ER",
                    phoneCode = "+291",
                    flag = "🇪🇷",
                    phoneFormat = "X XXX XXX"
                ),
                Country(
                    name = "Somalia",
                    code = "SO",
                    phoneCode = "+252",
                    flag = "🇸🇴",
                    phoneFormat = "XX XXX XXX"
                )
            )
        }
    }
    
    override fun toString(): String {
        return "$flag $name $phoneCode"
    }
}