package engine

import mechanics.V3

const val MAX_WIDTH = 100000.0

class Node(val c : V3, val width:Double){
    var cm = V3()
    var m = 0.0
    val members = mutableListOf<Int>()
    var children: Array<Node>? = null
    fun addPoint(pos : V3, mass:Double, index:Int ){
        m+=mass
        cm.x+=mass*pos.x
        cm.y+=mass*pos.y
        cm.z+=mass*pos.z
        members.add(index)
        if (members.size==2 && width > MAX_WIDTH){
            val d = width*0.5
            children = arrayOf(
                    Node(V3(c.x-d,c.y-d,c.z-d),d),
                    Node(V3(c.x-d,c.y-d,c.z+d),d),
                    Node(V3(c.x-d,c.y+d,c.z-d),d),
                    Node(V3(c.x-d,c.y+d,c.z+d),d),
                    Node(V3(c.x+d,c.y-d,c.z-d),d),
                    Node(V3(c.x+d,c.y-d,c.z+d),d),
                    Node(V3(c.x+d,c.y+d,c.z-d),d),
                    Node(V3(c.x+d,c.y+d,c.z+d),d)
            )

            val pos2 =V3(cm.x/m,cm.y/m,cm.z/m)
            val i = getNodeIndex(pos2 - c)
            children!![i].addPoint(pos2, m, members[0])
        }
        if (children!=null){
            val i = getNodeIndex(pos-c)
            children!![i].addPoint(pos,mass,index)
        }
    }

    private fun getNodeIndex(v3: V3): Int {
        var ret = 0
        if (v3.z>0) ret+=1
        if (v3.y>0) ret+=2
        if (v3.x>0) ret+=4
        return ret
    }

    fun normaliseCM() {
        if (m>0) {
            cm.x /= m
            cm.y /= m
            cm.z /= m
        }
        if (children!=null){
            children = children!!.filter{it.members.size>0}.toTypedArray()
            if (members.size<100) children=null
            else children!!.forEach { it.normaliseCM() }
        }

    }
}

class Tree(val width:Double, val radius:Double, val p: Array<P>, val mass1:Double, val dt:Double){
    fun calculateForces() {
        root.normaliseCM()
        for (i in p.withIndex()){
            doForces(root,i.value,i.index)
        }
    }


    val ANGLE_THRESHOLD = 0.2
    val RADIUS_ITERATE_FACTOR = 2.0
    val DECISION_ITERATE = 0
    val DECISION_SPLIT = 1
    val DECISION_FORCE = 2
    fun getDecision(n:Node, p:V3):Int{

        // Things that have to be true:
        // If the min dist is less than radius * 2, then I have to either iterate or split.
        // If the min dist is greater than radius * 2, then I have to either split or force.

        if (n.members.size==1) return DECISION_ITERATE
        val d = n.c dist p
        val minDist = d - n.width*1.8

        if (minDist < radius * RADIUS_ITERATE_FACTOR){
            if (d < radius * RADIUS_ITERATE_FACTOR){
                return DECISION_ITERATE
            }
            if (n.members.size<3){
                return DECISION_ITERATE
            }
            if (n.children==null) return DECISION_ITERATE
            return DECISION_SPLIT
        }
        else {
            val angle = n.width / d
            if (angle < ANGLE_THRESHOLD || n.members.size<5 || n.children==null) {
                return DECISION_FORCE
            }
            return DECISION_SPLIT
        }
    }

    private fun doForces(n:Node, i: P,pi:Int) {
        // Things we could do:
        // * Iterate over the members, Doing pair interactions everywhere.
        // * Force from all members to this point.
        // * Iterate over children
        val decision = getDecision(n,i.p)

        if (decision==DECISION_SPLIT) {
            for(j in n.children!!){
                doForces(j,i,pi)
            }
            return
        }
        if (decision==DECISION_FORCE){
            applyForce(n.cm, n.m, i)
            return
        }
        if (decision==DECISION_ITERATE){
            for(j in n.members){
                if (j==pi) continue
                val d2 = p[j].p dist2 i.p
                if (d2 > radius * radius * RADIUS_ITERATE_FACTOR*RADIUS_ITERATE_FACTOR){
                    applyForce(p[j].p,mass1, i)
                }
                else{
                    if (j>pi){
                        pairInteraction(i,p[j],dt,radius,mass1)
                    }
                }
            }
            return
        }
    }

    fun applyForce(cm: V3, mass:Double, i: P) {
        val dx = cm.x - i.p.x
        val dy = cm.y - i.p.y
        val dz = cm.z - i.p.z
        val d = Math.sqrt(dx * dx + dy * dy + dz * dz)
        val f = dt * GRAV_CONST * mass / (d * d * d)
        i.v.x += f * dx
        i.v.y += f * dy
        i.v.z += f * dz
    }

    val root = Node(V3(), width)
}