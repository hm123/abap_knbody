package engine

import display.show
import mechanics.*
import toBitmap
import java.io.File
import javax.imageio.ImageIO



class State(val p:Array<P>, val m1:Double, val radius:Double){
    var totalTime = 0.0
    var steps = 0

    // This function produces a Matrix3 containing a bitmap of the current state.
    fun display(): Matrix3 {
        val n = 800
        val ret = newMatrix3(n,n)
        val scale = 55e6
        for (i in p){
            val x = n/2 + n * i.p.x/scale
            val y = n/2 + n * i.p.y/scale
            val t= Math.sqrt(i.t)*1e-2

            val s = 0.10 * (5000.0 / p.size)
            ret.fillCircle(x,y,5.0,
                    Math.min(4.0,0.1+t)*s,
                    Math.min(4.0,0.1+t*0.08)*s,
                    Math.min(4.0,0.1+t*0.006)*s)
            ret.fillCircle(x,y,1.5,
                    Math.min(15.0,0.1+t)*s*6.0,
                    Math.min(15.0,0.1+t*0.08)*s*6.0,
                    Math.min(15.0, 0.1+t*0.006)*s*6.0)

        }
        return ret
    }

    // This function saves the image of the current state in the output directory, and
    // also puts it on the screen, depending on arguments.
    // It may be better to separate these two functions.
    var frame = 0
    fun output(outdir:String?, showOnScreen:Boolean){
        if (showOnScreen) show(display())
        if (outdir!=null) {
            val bmp = display().toBitmap()
            ImageIO.write(bmp, "png", File(outdir+"/out${1000000 + frame}.png"))
            //this.saveJson(outdir+"/jout${1000000+frame}.json")
        }
        frame++
    }

    // Update positions
    fun timestepP(dt:Double){
        for(i in p) i.p.addAndScaleIP(i.v, dt)
    }

    // Update velocities using N^2 method
    fun timestepVN2(dt:Double){
        for(i in 0 until p.size){
            for(j in 0 until i){
                pairInteraction(p[i],p[j],dt,radius,m1)
            }
        }
    }

    // Update velocities using the Barnes-Hut method
    fun timestepVBH(dt:Double){
        val b = Tree(SIZE,radius, p, m1, dt)
        for(i in 0 until p.size){
            b.root.addPoint(p[i].p, m1, i)
        }
        b.calculateForces()
    }

    // dt is the timestep time: 0.5 means that after calling this function, the simulation
    // will be +0.5s on its previous state.
    // n2 is a flag that determines whether the N^2 algorithm is used or the NLogN algorithm.
    // N^2 is more accurate and faster for smaller simulations, but NLogN is faster
    // for large simulations.

    fun timestep(dt:Double, n2:Boolean){
        totalTime+=dt
        steps++

        // This updates the positions.
        timestepP(dt)

        // This updates the velocities.
        if (n2)
            timestepVN2(dt)
        else
            timestepVBH(dt)
    }
}

// This function is used both by the N^2 and NLogN algorithms to model how two nearby particles interact.
// p1 is the first particle.
// p2 is the second particle.
// dt is the timestep length.
// radius is the radius of a particle in metres.
// m1 is the mass of 1 particle in kg.
// Note that this function writes out the vector calculations for speed
// It would be clearer if it used vector methods, but possibly take longer to run.

fun pairInteraction(p1: P, p2: P, dt: Double, radius:Double, m1:Double) {
    val p1p = p1.p
    val p2p = p2.p

    val p1v = p1.v
    val p2v = p2.v


    // This vector is the relative position of p2 to p1.
    val dx = p2p.x-p1p.x
    val dy = p2p.y-p1p.y
    val dz = p2p.z-p1p.z

    // This is the distance between the particles.
    val d = Math.sqrt(dx*dx+dy*dy+dz*dz + 0.0001 * radius*radius)

    // gravitational force (or will be when multiplied by (dx,dy,dz))
    val f2 = dt * (
            GRAV_CONST * m1 / (d*d*d)
            )
    // add in an outward force for particles too close.
    val g = f2 + dt * Math.min(0.0,d - 2.00*radius)/(radius*d) * RESTORING_FORCE

    // now convert to a force.
    val fx = dx * g
    val fy = dy * g
    val fz = dz * g

    // now update the velocities.
    p1v.x+=fx
    p1v.y+=fy
    p1v.z+=fz
    p2v.x-=fx
    p2v.y-=fy
    p2v.z-=fz

    // This is just so that the energy of each collision affects the colour of
    // the particle:
    if (d<radius*2.0){

        // normalised direction between particles.
        val wx=dx/d
        val wy=dy/d
        val wz=dz/d

        // relative velocity of v2 to v1.
        val dvx = p2v.x - p1v.x
        val dvy = p2v.y - p1v.y
        val dvz = p2v.z - p1v.z

        // collision speed.
        val vd = (wx*dvx+wy*dvy+wz*dvz)

        // update temperature of particles.

        p1.t+=vd*vd*0.010
        p2.t+=vd*vd*0.010

        p1.t*=0.9
        p2.t*=0.9

        // Old code that handled collisions.
        /*

        if (vd<0) {
            piv.x += wx * vd
            piv.y += wy * vd
            piv.z += wz * vd
            pjv.x -= wx * vd
            pjv.y -= wy * vd
            pjv.z -= wz * vd
        }
        */
    }
}

const val EARTH_MASS = 6e24       // kg
const val GRAV_CONST = 6.67e-11   // si units
const val RESTORING_FORCE = 500.0 // This is the acceleration of particles separated by 1 radius, in ms^-2
const val SIZE = 100e6            // This is the size of the node of the Barnes-Hut tree
const val dt = 0.5                // seconds

fun newSim(n :Int, outdir:String?, show:Boolean) : State {
    val pts = mutableListOf<P>()

    // number of points making up Earth
    val nEarth = (n*0.8).toInt()

    // number of points making up Theia
    val nOther = n - nEarth

    // in metres:
    val earthRadius = 6e6

    // in metres, the radius of each point.
    val ptRadius = 0.9 * earthRadius / Math.pow(nEarth.toDouble(),0.3333)

    // in kg, the mass of one particle
    val mass1 = EARTH_MASS / nEarth

    // a list of particles for planet Earth. Note that the following line is an entire simulation.
    val p1 = getPlanet(nEarth,earthRadius ,mass1, ptRadius, outdir, show, 2.6)

    // a list of particles for planet Theia. Note that the following line is an entire simulation.
    val p2 = getPlanet(
            nOther,
            earthRadius * Math.pow(nOther/nEarth.toDouble(),0.333),
            mass1,
            ptRadius,
            outdir,
            show,
            4.0)

    // how far apart the two planets are, in metres.
    val distApart = earthRadius*3

    // The speed of the collision. Note that the first part is the escape velocity
    // which is the speed they would be moving at if they started stationary far apart.
    // The last term is an additional energy from their relative orbital speeds.
    val speed = Math.sqrt(2 * GRAV_CONST * EARTH_MASS / distApart + 0.5 * 3.5e3*3.5e3)

    // The angle of attack at the start of the simulation.
    val theta = 0.55

    // Now update the 2nd planet according to the decisions above.
    p2.forEach{
        it.p.x+=distApart
        it.v.x-=speed * Math.cos(theta)
        it.v.y+=speed * Math.sin(theta)
    }

    // now add both planets to the main particle list.
    pts.addAll(p1)
    pts.addAll(p2)

    // now center the position and velocity of the particle list.
    val meanPos = pts.map{it.p}.foldRight(V3()) { x, y->x+y} * (1.0 / pts.size)
    val meanVel = pts.map{it.v}.foldRight(V3()) { x, y->x+y} * (1.0 / pts.size)

    pts.forEach{
        it.p.subIP(meanPos)
        it.v.subIP(meanVel)
    }

    // now return the state we've created.
    return State(pts.toTypedArray(), mass1, ptRadius)
}

// Here's an example of a simpler way to make a sim, if you don't care about making planets quickly.
fun newSimpleSim(n :Int) : State {
    val pts = mutableListOf<P>()

    // angular speed
    val w =3.1415*2 / (3600.0 * 0.2)
    while(pts.size<n){
        val x = (Math.random()-0.5)*2*6e6
        val y = (Math.random()-0.5)*2*6e6
        val z = (Math.random()-0.5)*2*6e6

        // random position
        val p =V3(x,y,z)

        // but rotating uniformly
        val v = p * V3(0.0,0.0,w)
        pts.add(P(p, v,0.0))
    }
    return State(pts.toTypedArray(), 6.4e24/n,6e6 / Math.pow(n.toDouble(),0.3333))
}

// This gets a particle list for a planet.
fun getPlanet(n: Int, rad: Double, mass1: Double, ptRadius: Double, outdir:String?, show:Boolean, periodHours: Double): List<P> {
    // main particle list
    val pts = mutableListOf<P>()
    // angular speed.
    val w =3.1415*2.0 / (3600.0 * -periodHours)

    // generate them in a sphere of the right radius.
    while(pts.size<n){
        val x = (Math.random()-0.5)*2*rad
        val y = (Math.random()-0.5)*2*rad
        val z = (Math.random()-0.5)*2*rad
        if (x*x+y*y+z*z > rad*rad) continue
        pts.add(P(V3(x,y,z), V3(),0.0))
    }

    // create the initial state for a simulation
    val sim = State(pts.toTypedArray(), mass1, ptRadius)

    // run steps to make the thing flow into a nice round planet.
    val nSteps = 3000
    for(i in 0 until nSteps){
        sim.timestep(dt,false)

        if ((i%5)==0) println("step $i of $nSteps ($n points)")
        if (i%30 == 0) sim.output(outdir, show)
        // Heavy damping in the first 150 steps --- first 2.5 minutes.
        if (i<150){
            sim.p.map{it.v.scaleIP(0.8)}
            sim.p.map{it.t*=0.9}
        }
        // Less heavy damping after that. Also spins it up to the right angular speed.
        if (i<nSteps*0.8) {
            sim.p.map {
                val v2 = it.p * V3(0.0, 0.0, w)
                val a = 0.99
                val v3 = it.v * a + v2 * (1 - a)
                it.v.x = v3.x
                it.v.y = v3.y
                it.v.z = v3.z
                it.t*=0.5
            }
        }
    }
    return sim.p.toList()
}
