package eshop.utils

object TimeFormatter {

  def formatDuration(milliseconds: Long): String = {
    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val remainingSeconds = totalSeconds % 60

    val hoursString = if (hours > 0 || minutes > 0) f"$hours%dh " else ""
    val minutesString = f"$minutes%dm "
    val secondsString = f"$remainingSeconds%ds"

    hoursString + minutesString + secondsString
  }

}
