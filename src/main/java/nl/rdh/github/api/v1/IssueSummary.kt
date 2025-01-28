package nl.rdh.github.api.v1

data class IssueSummary(val url:String, val title:String, val state: String, val comments: Int, val labels:List<String>)
