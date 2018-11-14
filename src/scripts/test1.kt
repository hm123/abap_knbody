package scripts

import engine.State
import engine.dt
import engine.newSim
import mechanics.loadJson
import mechanics.saveJson
import java.io.File


fun main(args:Array<String>){
    println("Version 2018-11-14")
    //Set default parameters.
    var outdir : String? = null
    var n = 1500
    var useLastSim = false
    var show = true

    //Parse the command line arguments
    for(i in 0 until args.size){
        if (args[i]=="-o") outdir = args[i+1]
        if (args[i]=="-n") n = args[i+1].toInt()
        if (args[i]=="-r") useLastSim = true
        if (args[i]=="-ns") show = false
    }

    println("Saving to $outdir")
    if (outdir!=null) File(outdir).mkdirs()
    println("n=$n")
    println(if(useLastSim) "loading previous sim" else "new sim")

    // Now we have to generate an initial state, called s here.
    var s : State? = null
    val fn = "$outdir/initial.json"
    if (useLastSim) {
        if (outdir==null) throw Exception("null outdir, but use last sim are incompatible")
        s = fn.loadJson(State::class.java)
    }
    else{
        File("$outdir/planets").mkdirs()
        // This is where the initial state is generated if not reused.
        // This actually carries out its own simulations to form the initial planets.
        // These mini-simulations get saved in outdir/planets.

        s = newSim(n, if (outdir==null) null else "$outdir/planets", show)
        if (outdir!=null) s.saveJson("$outdir/initial.json")
    }
    if (s.p.size!=n) throw Exception("You specified using the last sim, but n is different.")

    // Now we have the main simulation loop, with 100k steps, producing 3333 frames.
    for(i in 0 until 100000) {
        if ((i%5)==0) println("$i")
        s.timestep(dt, false)
        if (i%30 == 0) s.output(outdir, show)
    }
}
