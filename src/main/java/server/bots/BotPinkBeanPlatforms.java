package server.bots;
import java.awt.Point;
/** Outer platform intent during the native inter-wave warning, with ordinary arrival tolerance. */
final class BotPinkBeanPlatforms {
 private BotPinkBeanPlatforms() {}
 static Point adjustment(Point p,int wave){
  if(p.y< -110||p.y> -90)return null;
  if(wave>=3&&p.x> -515&&p.x<=-384)return new Point(-530,-99);
  if(wave>=4&&p.x>=389&&p.x<525)return new Point(540,-102);
  return null;
 }
}
