/*
 *
 *                    | ->  Node3 -->|
 *        Node1--> -->| ->  Node3 -->| -> Node4
 *                    | ->  Node3 -->|
 *   /<-- pipe0-->/   /<--------- a2a -------->/
 *         G1                     G2
 *   /<----------------- pipe ---------------->/
 *
 *  G1: pipe0
 *  G2: a2a
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
        std::cout << "Source exiting!" << std::endl;
        return EOS;
    }
	const long ntasks;
};

struct Node3: ff_node_t<myTask_t>{
    myTask_t* svc(myTask_t* t){
		t->num  += get_my_id();
        return t;
    }
	void eosnotify(ssize_t) {
		printf("Node3 %ld EOS RECEIVED\n", get_my_id());
		fflush(NULL);
	}

};

struct Node4: ff_minode_t<myTask_t>{
	Node4(long ntasks):ntasks(ntasks) {}
    myTask_t* svc(myTask_t* t){
		//std::cout << "Node4: from (" << get_channel_id() << ") " << t->str << " (" << t->S.t << ", " << t->S.f << ")\n";
		++processed;
		delete t;
        return GO_ON;
    }
	void eosnotify(ssize_t id) {
		printf("Node4 EOS RECEIVED from %ld\n", id);
		fflush(NULL);
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
	}

    ff_pipeline pipe;
	Node1 n1(ntasks);
	Node3 n31, n32, n33;
	Node4 n4(ntasks);
	ff_pipeline pipe0;
	pipe0.add_stage(&n1);
	ff_a2a      a2a;
	a2a.add_firstset<Node3>({&n31, &n32, &n33});
    a2a.add_secondset<Node4>({&n4});
	pipe.add_stage(&pipe0);
	pipe.add_stage(&a2a);

    auto G1 = pipe0.createGroup("G1");
    auto G2 = a2a.createGroup("G2");
    auto G3 = a2a.createGroup("G3");

    G1.out << &n1;
    G2.in  << &n31 << &n32 << &n33;
	G2.out << &n31 << &n32 << &n33;
    G3.in << &n4;

	unsigned long inizio=getusec();
	if (pipe.run_and_wait_end()<0) {
		error("running the main pipe\n");
		return -1;
	}
	unsigned long fine=getusec();
    std::cout << "TEST  Time = " << (fine-inizio) / 1000.0 << " ms\n";
	return 0;
}
