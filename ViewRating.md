# seeing #

  * none = no seeing (daylight), this forces overall rating to 0
  * 0 = worst
  * 5 = best

# transparency #

  * 0 = worst
  * 5 = best

# clouds #

  * 10 = worst
  * 0 = best

# view rating #

  * viewRating = (seeing == 'none') ? 0 : seeing + trans + cloud
  * max value is 20, min value is -1
  * excellent = 16-20, good = 11-16, fair = 6-10, poor = 1-5, none = 0