/*
 *
 *   Node1-->Node2 --> Node3 --> Node4
 *
 *   /<-- pipe0-->/    /<---pipe1--->/
 *   /<----------- pipe ------------>/
 *
 *  G1: pipe0
 *  G2: pipe1
 */


#include <iostream>
#include <ff/dff.hpp>

using namespace ff;

struct myTask_t {
	long num;

	template<class Archive>
	void serialize(Archive & archive) {
		archive(num);
	}

};


struct Node1: ff_monode_t<myTask_t>{
	Node1(long ntasks):ntasks(ntasks) {}
    myTask_t* svc(myTask_t*){
        for(long i=0; i< ntasks; i++) {
			myTask_t* task = new myTask_t;
			task->num=i;
            ff_send_out(task);
		}
        return EOS;
    }
	const long ntasks;
};

struct Node4: ff_minode_t<myTask_t>{
	Node4(long ntasks):ntasks(ntasks) {}
    myTask_t* svc(myTask_t* t){
		//std::cout << "Node4: from (" << get_channel_id() << ") " << t->str << " (" << t->S.t << ", " << t->S.f << ")\n";
		++processed;
		t->num *= 2;
        return GO_ON;
    }
	void svc_end() {
		if (processed != ntasks) {
			abort();
		}
		std::cout << "RESULT OK\n";
	}
	long ntasks;
	long processed=0;
};


int main(int argc, char*argv[]){
    if (DFF_Init(argc, argv)<0 ) {
		error("DFF_Init\n");
		return -1;
	}
	long ntasks = 1000;
	if (argc>1) {
		ntasks = std::stol(argv[1]);
		std::cout << "Processing "<<ntasks << " tasks" << std::endl;
	}

    ff_pipeline pipe;
	Node1 n1(ntasks);
	Node4 n4(ntasks);
	ff_pipeline pipe0;
	pipe0.add_stage(&n1);
	ff_pipeline pipe1;
	pipe1.add_stage(&n4);
	pipe.add_stage(&pipe0);
	pipe.add_stage(&pipe1);

    auto G1 = pipe0.createGroup("G1");
    auto G2 = pipe1.createGroup("G2");

    G1.out << &n1;
    G2.in  << &n4;

	unsigned long inizio=getusec();
	if (pipe.run_and_wait_end()<0) {
		error("running the main pipe\n");
		return -1;
	}
	unsigned long fine=getusec();
    std::cout << "TEST  Time = " << (fine-inizio) / 1000.0 << " ms\n";
	return 0;
}
