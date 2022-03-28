export const formatTime = (time: number) => {
  const seconds = time % 60;
  const minutes = Math.floor(time / 60);

  const formattedMinutes = minutes < 10 ? `0${minutes}` : minutes;
  const formattedSeconds = seconds < 10 ? `0${seconds}` : seconds;

  return `${formattedMinutes}:${formattedSeconds}`;
};
